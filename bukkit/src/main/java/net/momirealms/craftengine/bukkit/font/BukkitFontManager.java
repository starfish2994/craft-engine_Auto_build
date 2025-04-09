package net.momirealms.craftengine.bukkit.font;

import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.font.AbstractFontManager;
import net.momirealms.craftengine.core.font.FontManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.text.minimessage.NamedArgumentTag;
import net.momirealms.craftengine.core.util.CharacterUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
    private void processChatEvent(AsyncChatDecorateEvent event) {
        Player player = event.player();
        if (player == null) return;
        try {
            Object originalMessage = Reflections.field$AsyncChatDecorateEvent$originalMessage.get(event);
            String rawJsonMessage = ComponentUtils.paperAdventureToJson(originalMessage);
            String jsonMessage = replaceEmoji(rawJsonMessage, this.plugin.adapt(player));
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
