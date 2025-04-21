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
                    for (Player player : players) {
                        player.sendMessage("Better Together! This server supports 20 players (Community Edition). Want more slots & features? Ask the admin about: » Going CraftEngine Premium Edition!");
                    }
                    this.plugin.logger().warn("Glad to see that your server is growing!");
                    this.plugin.logger().warn("The Community Edition supports up to 20 players. Unlock limitless potential with CraftEngine Premium:");
                    this.plugin.logger().warn("► Unlimited player capacity");
                    this.plugin.logger().warn("► Priority support");
                    this.plugin.logger().warn("► Advanced management tools");
                }
            }, 1, 1, TimeUnit.MINUTES);
            this.plugin.onPluginEnable();
            this.plugin.logger().warn("You're using the CraftEngine Community Edition");
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
