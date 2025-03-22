package net.momirealms.craftengine.bukkit.font;

import com.google.gson.JsonElement;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.font.AbstractImageManager;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.CharacterUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.lang.reflect.InvocationTargetException;

public class BukkitImageManager extends AbstractImageManager implements Listener {
    private final BukkitCraftEngine plugin;
    private final Object serializer;

    public BukkitImageManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        try {
            Object builder = Reflections.method$GsonComponentSerializer$builder.invoke(null);
            this.serializer = Reflections.method$GsonComponentSerializer$Builder$build.invoke(builder);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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

    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onChat(AsyncChatDecorateEvent event) {
        if (event.player() == null) return;
        this.ProcessChatMessages(event);
    }

    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onChatCommand(AsyncChatCommandDecorateEvent event) {
        if (event.player() == null) return;
        this.ProcessChatMessages(event);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        event.setMessage(processIllegalString(event.getMessage()));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void ProcessChatMessages(AsyncChatDecorateEvent event) {
        try {
            Object originalMessage = Reflections.clazz$AdventureComponent.cast(Reflections.field$AsyncChatDecorateEvent$originalMessage.get(event));
            JsonElement json = (JsonElement) Reflections.method$GsonComponentSerializer$serializeToTree.invoke(serializer, originalMessage);
            String jsonMessage = AdventureHelper.jsonElementToStringJson(json);
            if (!this.isDefaultFontInUse()) return;
            String str = processIllegalString(jsonMessage);
            Object component = Reflections.method$ComponentSerializer$deserialize.invoke(serializer, str);
            Reflections.method$AsyncChatDecorateEvent$result.invoke(event, component);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String processIllegalString(String string) {
        char[] chars = string.toCharArray();
        int[] codepoints = CharacterUtils.charsToCodePoints(chars);
        int[] newCodepoints = new int[codepoints.length];
        for (int i = 0; i < codepoints.length; i++) {
            int codepoint = codepoints[i];
            if (!this.isIllegalCharacter(codepoint)) {
                newCodepoints[i] = codepoint;
            } else {
                newCodepoints[i] = '*';
            }
        }
        return new String(newCodepoints, 0, newCodepoints.length);
    }
}
