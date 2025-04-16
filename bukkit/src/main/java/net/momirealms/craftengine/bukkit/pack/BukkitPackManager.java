package net.momirealms.craftengine.bukkit.pack;

import net.momirealms.craftengine.bukkit.api.event.AsyncResourcePackGenerateEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.pack.AbstractPackManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Optional;
import java.util.UUID;

public class BukkitPackManager extends AbstractPackManager implements Listener {
    private final BukkitCraftEngine plugin;

    public BukkitPackManager(BukkitCraftEngine plugin) {
        super(plugin, (rf, zp) -> {
            AsyncResourcePackGenerateEvent endEvent = new AsyncResourcePackGenerateEvent(rf, zp);
            EventUtils.fireAndForget(endEvent);
        });
        this.plugin = plugin;
    }

    @Override
    public void delayedInit() {
        super.delayedInit();
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // todo 1.20.1 资源包发送
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        // for 1.20.1 servers, not recommended to use
        if (Config.sendPackOnJoin() && Config.kickOnDeclined() && !VersionHelper.isVersionNewerThan1_20_2()) {
            if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED || event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
                event.getPlayer().kick();
            }
        }
    }

    @Override
    public void load() {
        if (Config.sendPackOnJoin()) {
            this.modifyServerSettings();
        }
    }

    public void modifyServerSettings() {
        try {
            Object settings = Reflections.field$DedicatedServer$settings.get(Reflections.method$MinecraftServer$getServer.invoke(null));
            Object properties = Reflections.field$DedicatedServerSettings$properties.get(settings);
            Object info;
            if (VersionHelper.isVersionNewerThan1_20_3()) {
                info = Reflections.constructor$ServerResourcePackInfo.newInstance(new UUID(0, 0), "", "", Config.kickOnDeclined(), ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt()));
            } else {
                info = Reflections.constructor$ServerResourcePackInfo.newInstance(new UUID(0, 0), "", Config.kickOnDeclined(), ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt()));
            }
            Reflections.field$DedicatedServerProperties$serverResourcePackInfo.set(properties, Optional.of(info));
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to update resource pack settings", e);
        }
    }

    @Override
    public void unload() {
        super.unload();
        this.resetServerSettings();
    }

    @Override
    public void disable() {
        super.disable();
        HandlerList.unregisterAll(this);
        this.resetServerSettings();
    }

    public void resetServerSettings() {
        try {
            Object settings = Reflections.field$DedicatedServer$settings.get(Reflections.method$MinecraftServer$getServer.invoke(null));
            Object properties = Reflections.field$DedicatedServerSettings$properties.get(settings);
            Reflections.field$DedicatedServerProperties$serverResourcePackInfo.set(properties, Optional.empty());
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to reset resource pack settings", e);
        }
    }

    @Override
    public void generateResourcePack() {
        // generate pack
        super.generateResourcePack();
    }
}
