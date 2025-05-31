package net.momirealms.craftengine.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperCraftEnginePlugin extends JavaPlugin {
    private final PaperCraftEngineBootstrap bootstrap;
    private boolean hasLoaded = false;

    public PaperCraftEnginePlugin(PaperCraftEngineBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.bootstrap.plugin.setJavaPlugin(this);
    }

    @Override
    public void onLoad() {
        if (!this.hasLoaded) {
            this.hasLoaded = true;
            this.bootstrap.plugin.onPluginLoad();
        }
    }

    @Override
    public void onEnable() {
        this.bootstrap.plugin.onPluginEnable();
    }

    @Override
    public void onDisable() {
        this.bootstrap.plugin.onPluginDisable();
    }
}
