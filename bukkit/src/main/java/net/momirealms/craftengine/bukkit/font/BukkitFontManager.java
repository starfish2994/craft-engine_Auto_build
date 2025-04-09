package net.momirealms.craftengine.bukkit.font;

import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.PlayerSignCommandPreprocessEvent;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.font.AbstractFontManager;
import net.momirealms.craftengine.core.font.FontManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.CharacterUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
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
        if (event.getPlayer().hasPermission(FontManager.BYPASS_COMMAND)) {
            return;
        }
        runIfContainsIllegalCharacter(event.getMessage(), event::setMessage);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void processChatEvent(AsyncChatDecorateEvent event) {
        Player player = event.player();
        if (player == null) return;
        try {
            Object originalMessage = Reflections.field$AsyncChatDecorateEvent$originalMessage.get(event);
            String jsonMessage = ComponentUtils.paperAdventureToJson(originalMessage);
            if (!player.hasPermission(FontManager.BYPASS_CHAT))  {
                runIfContainsIllegalCharacter(jsonMessage, (json) -> {
                    Object component = ComponentUtils.jsonToPaperAdventure(json);
                    try {
                        Reflections.method$AsyncChatDecorateEvent$result.invoke(event, component);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void runIfContainsIllegalCharacter(String raw, Consumer<String> callback) {
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
                callback.accept(new String(newCodepoints, 0, newCodepoints.length));
            }
        } else if (hasIllegal) {
            callback.accept(raw);
        }
    }
}
