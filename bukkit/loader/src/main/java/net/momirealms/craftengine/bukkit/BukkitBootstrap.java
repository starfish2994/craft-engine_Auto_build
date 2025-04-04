package net.momirealms.craftengine.bukkit;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitBootstrap extends JavaPlugin {
    private final BukkitCraftEngine plugin;

    public BukkitBootstrap() {
        this.plugin = new BukkitCraftEngine(this);
    }

    @Override
    public void onLoad() {
        this.plugin.onPluginLoad();
    }

    @Override
    public void onEnable() {
        this.plugin.onPluginEnable();
    }

    @Override
    public void onDisable() {
        this.plugin.onPluginDisable();
    }
}
