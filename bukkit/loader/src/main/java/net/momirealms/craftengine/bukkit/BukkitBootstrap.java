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
            Bukkit.getPluginManager().disablePlugin(this);
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
            this.plugin.logger().warn("You're using the CraftEngine Community Edition");
            this.plugin.logger().warn(" - Commercial use on production servers is prohibited");
            this.plugin.logger().warn(" - You must enable server's online mode");
            this.plugin.logger().warn(" - Maximum player limit is restricted to 20");
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
