package net.momirealms.craftengine.bukkit.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class PaperCraftEnginePlugin extends JavaPlugin {
    private final PaperCraftEngineBootstrap bootstrap;

    public PaperCraftEnginePlugin(PaperCraftEngineBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.bootstrap.plugin.setJavaPlugin(this);
    }

    @Override
    public void onLoad() {
        this.bootstrap.plugin.onPluginLoad();
    }

    @Override
    public void onEnable() {
        this.bootstrap.plugin.onPluginEnable();
        this.bootstrap.plugin.scheduler().asyncRepeating(() -> {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            if (players.size() > 20) {
                for (Player player : players) {
                    player.sendMessage("Better Together! This server supports 20 players (Community Edition). Want more slots & features? Ask the admin about: » Going CraftEngine Premium Edition!");
                }
                this.bootstrap.plugin.logger().warn("Glad to see that your server is growing!");
                this.bootstrap.plugin.logger().warn("The Community Edition supports up to 20 players. Unlock limitless potential with CraftEngine Premium:");
                this.bootstrap.plugin.logger().warn("► Unlimited player capacity");
                this.bootstrap.plugin.logger().warn("► Priority support");
                this.bootstrap.plugin.logger().warn("► Advanced management tools");
            }
        }, 1, 1, TimeUnit.MINUTES);
        this.bootstrap.plugin.onPluginEnable();
        this.bootstrap.plugin.logger().warn("You're using the CraftEngine Community Edition");
        this.bootstrap.plugin.logger().warn(" - Maximum player limit is restricted to 20");
    }

    @Override
    public void onDisable() {
        this.bootstrap.plugin.onPluginDisable();
    }
}
