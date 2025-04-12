package net.momirealms.craftengine.bukkit;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BukkitBootstrap extends JavaPlugin {
    private final BukkitCraftEngine plugin;

    public BukkitBootstrap() {
        this.plugin = new BukkitCraftEngine(this);
    }

    @Override
    public void onLoad() {
        if (!Bukkit.getServer().getOnlineMode()) {
            return;
        }
        this.plugin.onPluginLoad();
    }

    @Override
    public void onEnable() {
        if (!Bukkit.getServer().getOnlineMode()) {
            this.plugin.logger().warn("CraftEngine Community Edition requires online mode to be enabled.");
        } else {
            this.plugin.scheduler().asyncRepeating(() -> {
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                if (players.size() > 20) {
                    this.plugin.logger().warn("CraftEngine Community Edition restricts servers to a maximum of 20 concurrent players.");
                    this.plugin.logger().warn("Your server has exceeded this limit and will be shut down.");
                    Bukkit.shutdown();
                }
            }, 1, 1, TimeUnit.MINUTES);
            this.plugin.onPluginEnable();
        }
    }

    @Override
    public void onDisable() {
        if (!Bukkit.getServer().getOnlineMode()) {
            return;
        }
        this.plugin.onPluginDisable();
    }
}
