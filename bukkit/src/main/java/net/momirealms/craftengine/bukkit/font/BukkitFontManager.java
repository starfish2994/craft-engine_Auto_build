package net.momirealms.craftengine.bukkit.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.LegacyInventoryUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.font.AbstractFontManager;
import net.momirealms.craftengine.core.font.Emoji;
import net.momirealms.craftengine.core.font.EmojiParameters;
import net.momirealms.craftengine.core.font.FontManager;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.util.context.PlayerContext;
import org.ahocorasick.trie.Token;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.view.AnvilView;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

public class BukkitFontManager extends AbstractFontManager implements Listener {
    private final BukkitCraftEngine plugin;

    public BukkitFontManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
    }

    @Override
    public void disable() {
        super.disable();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void delayedLoad() {
        Map<UUID, String> oldCachedEmojiSuggestions = this.oldCachedEmojiSuggestions();
        super.delayedLoad();
        this.oldCachedEmojiSuggestions.putAll(this.cachedEmojiSuggestions());
        Bukkit.getOnlinePlayers().forEach(player -> {
            FastNMS.INSTANCE.method$ChatSuggestions$remove(oldCachedEmojiSuggestions.keySet(), player);
            this.addEmojiSuggestions(player);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.addEmojiSuggestions(player);
    }

    private void addEmojiSuggestions(Player player) {
        Map<UUID, String> hasPermissions = new HashMap<>();
        Map<UUID, String> cachedEmojiSuggestions = this.cachedEmojiSuggestions();
        for (UUID uuid : cachedEmojiSuggestions.keySet()) {
            String keyword = cachedEmojiSuggestions.get(uuid);
            Emoji emoji = super.emojiMapper.get(keyword);
            if (emoji == null) continue;
            if (emoji.permission() != null && !player.hasPermission(Objects.requireNonNull(emoji.permission()))) {
                continue;
            }
            hasPermissions.put(uuid, keyword);
        }
        FastNMS.INSTANCE.method$ChatSuggestions$add(hasPermissions, player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("UnstableApiUsage")
    public void onChat(AsyncChatDecorateEvent event) {
        if (!Config.filterChat()) return;
        this.processChatEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("UnstableApiUsage")
    public void onChatCommand(AsyncChatCommandDecorateEvent event) {
        if (!Config.filterChat()) return;
        this.processChatEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!Config.filterCommand()) return;
        if (!event.getPlayer().hasPermission(FontManager.BYPASS_COMMAND)) {
            IllegalCharacterProcessResult result = processIllegalCharacters(event.getMessage());
            if (result.has()) {
                event.setMessage(result.newText());
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilRename(PrepareAnvilEvent event) {
        if (super.emojiKeywordTrie == null) {
            return;
        }
        ItemStack result = event.getResult();
        if (result == null) return;
        Player player;
        try {
            player = (Player) Reflections.method$InventoryView$getPlayer.invoke(VersionHelper.isVersionNewerThan1_21() ? event.getView() : LegacyInventoryUtils.getView(event));
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to get inventory viewer", e);
            return;
        }

        String renameText;
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            AnvilView anvilView = event.getView();
            renameText = anvilView.getRenameText();
        } else {
            renameText = LegacyInventoryUtils.getRenameText(event.getInventory());
        }

        if (renameText == null || renameText.isEmpty()) return;

        Component itemName = Component.text(renameText);
        MutableInt parsedCount = new MutableInt(0);
        processComponent(itemName, player, parsedCount, (text) -> {
            if (parsedCount.value >= Config.maxEmojiParsed()) return;
            Item<ItemStack> wrapped = this.plugin.itemManager().wrap(result);
            wrapped.customName(AdventureHelper.componentToJson(text));
            event.setResult(wrapped.loadCopy());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (!event.isSigning()) return;
        Player player = event.getPlayer();
        BookMeta newBookMeta = event.getNewBookMeta();
        List<?> pages = newBookMeta.pages();
        MutableBoolean replacedBookMeta = new MutableBoolean(false);
        MutableInt parsedCount = new MutableInt(0);
        for (int i = 0; i < pages.size(); i++) {
            int finalIndex = i;
            JsonElement json = ComponentUtils.paperAdventureToJsonElement(pages.get(i));
            if (json instanceof JsonPrimitive primitive) {
                if (primitive.isString() && primitive.getAsString().isEmpty()) continue;
            }
            Component page = AdventureHelper.jsonElementToComponent(json);
            processComponent(page, player, parsedCount, (text) -> {
                try {
                    replacedBookMeta.value = true;
                    Reflections.method$BookMeta$page.invoke(
                            newBookMeta, finalIndex + 1,
                            ComponentUtils.jsonElementToPaperAdventure(AdventureHelper.componentToJsonElement(text))
                    );
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.plugin.logger().warn("Failed to set book page", e);
                }
            });
            if (parsedCount.value > Config.maxEmojiParsed()) break;
        }
        if (replacedBookMeta.value) {
            event.setNewBookMeta(newBookMeta);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        List<Component> lines = event.lines();
        MutableInt parsedCount = new MutableInt(0);
        for (int i = 0; i < lines.size(); i++) {
            int finalIndex = i;
            JsonElement json = ComponentUtils.paperAdventureToJsonElement(lines.get(i));
            if (json.toString().isEmpty()) continue;
            Component line = AdventureHelper.jsonElementToComponent(json);
            processComponent(line, player, parsedCount, (text) -> {
                try {
                    Reflections.method$SignChangeEvent$line.invoke(
                            event, finalIndex,
                            ComponentUtils.jsonElementToPaperAdventure(AdventureHelper.componentToJsonElement(text))
                    );
                } catch (IllegalAccessException | InvocationTargetException e) {
                    plugin.logger().warn("Failed to set sign line", e);
                }
            });
            if (parsedCount.value > Config.maxEmojiParsed()) break;
        }
    }

    private void processComponent(Component text, Player player, MutableInt parsedCount, Consumer<Component> consumer) {
        if (parsedCount.value > Config.maxEmojiParsed()) return;
        Component textReplaced = text;
        Set<String> processedKeywords = new HashSet<>();
        for (Token token : super.emojiKeywordTrie.tokenize(AdventureHelper.componentToJson(text))) {
            if (!token.isMatch()) continue;
            if (parsedCount.value++ > Config.maxEmojiParsed()) return;
            String keyword = token.getFragment();
            if (processedKeywords.contains(keyword)) continue;
            Emoji emoji = super.emojiMapper.get(keyword);
            if (emoji == null) continue;
            if (emoji.permission() != null && !player.hasPermission(Objects.requireNonNull(emoji.permission()))) {
                continue;
            }
            textReplaced = textReplaced.replaceText(builder -> {
                builder.matchLiteral(keyword)
                        .replacement(AdventureHelper.miniMessage().deserialize(
                                emoji.content(),
                                PlayerContext.of(plugin.adapt(player), ContextHolder.builder()
                                        .withOptionalParameter(EmojiParameters.EMOJI, emoji.emojiImage())
                                        .withParameter(EmojiParameters.KEYWORD, emoji.keywords().get(0))
                                        .build()).tagResolvers()
                        ));
            });
            consumer.accept(textReplaced);
            processedKeywords.add(keyword);
        }
    }

    private static final class MutableInt {
        int value;
        MutableInt(int value) { this.value = value; }
    }

    private static final class MutableBoolean {
        boolean value;
        MutableBoolean(boolean value) { this.value = value; }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void processChatEvent(AsyncChatDecorateEvent event) {
        Player player = event.player();
        if (player == null) return;
        try {
            Object originalMessage = Reflections.field$AsyncChatDecorateEvent$originalMessage.get(event);
            String rawJsonMessage = ComponentUtils.paperAdventureToJson(originalMessage);
            String jsonMessage = replaceJsonEmoji(rawJsonMessage, this.plugin.adapt(player));
            boolean hasChanged = !rawJsonMessage.equals(jsonMessage);
            if (!player.hasPermission(FontManager.BYPASS_CHAT))  {
                IllegalCharacterProcessResult result = processIllegalCharacters(jsonMessage);
                if (result.has()) {
                    Object component = ComponentUtils.jsonToPaperAdventure(result.newText());
                    Reflections.method$AsyncChatDecorateEvent$result.invoke(event, component);
                } else if (hasChanged) {
                    Object component = ComponentUtils.jsonToPaperAdventure(jsonMessage);
                    Reflections.method$AsyncChatDecorateEvent$result.invoke(event, component);
                }
            } else if (hasChanged) {
                Object component = ComponentUtils.jsonToPaperAdventure(jsonMessage);
                Reflections.method$AsyncChatDecorateEvent$result.invoke(event, component);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private IllegalCharacterProcessResult processIllegalCharacters(String raw) {
        boolean hasIllegal = false;
        // replace illegal image usage
        Map<String, Component> tokens = matchTags(raw);
        if (!tokens.isEmpty()) {
            for (Map.Entry<String, Component> entry : tokens.entrySet()) {
                raw = raw.replace(entry.getKey(), "*");
                hasIllegal = true;
            }
        }

        if (this.isDefaultFontInUse()) {
            // replace illegal codepoint
            char[] chars = raw.toCharArray();
            int[] codepoints = CharacterUtils.charsToCodePoints(chars);
            int[] newCodepoints = new int[codepoints.length];

            for (int i = 0; i < codepoints.length; i++) {
                int codepoint = codepoints[i];
                if (!isIllegalCodepoint(codepoint)) {
                    newCodepoints[i] = codepoint;
                } else {
                    newCodepoints[i] = '*';
                    hasIllegal = true;
                }
            }

            if (hasIllegal) {
                return IllegalCharacterProcessResult.has(new String(newCodepoints, 0, newCodepoints.length));
            }
        } else if (hasIllegal) {
            return IllegalCharacterProcessResult.has(raw);
        }
        return IllegalCharacterProcessResult.not();
    }

    public record IllegalCharacterProcessResult(boolean has, String newText) {

        public static IllegalCharacterProcessResult has(String newText) {
            return new IllegalCharacterProcessResult(true, newText);
        }

        public static IllegalCharacterProcessResult not() {
            return new IllegalCharacterProcessResult(false, null);
        }
    }
}
