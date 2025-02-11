package net.momirealms.craftengine.core.plugin.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.PluginProperties;
import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager implements Reloadable {
    private static ConfigManager instance;
    protected final CraftEngine plugin;
    private YamlDocument config;
    protected String configVersion;

    public YamlDocument settings() {
        if (config == null) {
            throw new IllegalStateException("Main config not loaded");
        }
        return config;
    }

    protected boolean debug;
    protected boolean checkUpdate;
    protected boolean metrics;
    protected boolean resourcePack$overrideUniform;
    protected boolean resourcePack$protection$breakFormat;
    protected boolean hasPAPI;
    protected boolean forceUpdateLight;
    protected boolean enableLightSystem;
    protected int maxChainUpdate;
    protected boolean removeInvalidFurniture;
    protected Set<String> furnitureToRemove;
    protected float packMinVersion;
    protected float packMaxVersion;
    protected boolean enableSoundSystem;
    protected boolean enableRecipeSystem;

    public ConfigManager(CraftEngine plugin) {
        this.plugin = plugin;
        instance = this;
    }

    @Override
    public void load() {
        configVersion = PluginProperties.getValue("config");
        config = plugin.loadYamlConfig(
                "config.yml",
                GeneralSettings.builder()
                        .setRouteSeparator('.')
                        .setUseDefaults(false)
                        .build(),
                LoaderSettings
                        .builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.builder()
                        .setEscapeUnprintable(false)
                        .setScalarFormatter((tag, value, role, def) -> {
                            if (role == NodeRole.KEY) {
                                return ScalarStyle.PLAIN;
                            } else {
                                return tag == Tag.STR ? ScalarStyle.DOUBLE_QUOTED : ScalarStyle.PLAIN;
                            }
                        })
                        .build(),
                UpdaterSettings
                        .builder()
                        .setVersioning(new BasicVersioning("config-version"))
                        .build()
        );
        loadSettings();
    }

    @Override
    public void unload() {
        Reloadable.super.unload();
    }

    private void loadSettings() {
        YamlDocument config = settings();
        plugin.translationManager().forceLocale(TranslationManager.parseLocale(config.getString("force-locale", "")));
        // Basics
        debug = config.getBoolean("debug", false);
        metrics = config.getBoolean("metrics", false);
        checkUpdate = config.getBoolean("update-checker", false);
        // resource pack
        resourcePack$overrideUniform = config.getBoolean("resource-pack.override-uniform-font", false);
        packMinVersion = getVersion(config.get("resource-pack.supported-version.min", "1.20").toString());
        packMaxVersion = getVersion(config.get("resource-pack.supported-version.max", "LATEST").toString());
        // performance
        maxChainUpdate = config.getInt("performance.max-block-chain-update-limit", 64);
        forceUpdateLight = config.getBoolean("performance.light-system.force-update-light", false);
        enableLightSystem = config.getBoolean("performance.light-system.enable", true);
        // compatibility
        hasPAPI = plugin.isPluginEnabled("PlaceholderAPI");
        // furniture
        removeInvalidFurniture = config.getBoolean("furniture.remove-invalid-furniture-on-chunk-load.enable", false);
        furnitureToRemove = new HashSet<>(config.getStringList("furniture.remove-invalid-furniture-on-chunk-load.list"));
        // block
        enableSoundSystem = config.getBoolean("block.sound-system.enable", true);
        // recipe
        enableRecipeSystem = config.getBoolean("recipe.enable", true);

        Class<?> modClazz = ReflectionUtils.getClazz("net.momirealms.craftengine.mod.CraftEnginePlugin");
        if (modClazz != null) {
            Method setMaxChainMethod = ReflectionUtils.getStaticMethod(modClazz, new String[] {"setMaxChainUpdate"}, void.class, int.class);
            try {
                assert setMaxChainMethod != null;
                setMaxChainMethod.invoke(null, maxChainUpdate);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.logger().warn("Failed to set max chain update", e);
            }
        }
    }

    private static float getVersion(String version) {
        if (version.equalsIgnoreCase("LATEST")) {
            version = PluginProperties.getValue("latest-version");
        }
        String[] split = version.split("\\.", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        return Float.parseFloat(split[1]);
    }

    public static String configVersion() {
        return instance.configVersion;
    }

    public static boolean debug() {
        return instance.debug;
    }

    public static boolean checkUpdate() {
        return instance.checkUpdate;
    }

    public static boolean metrics() {
        return instance.metrics;
    }

    public static boolean hasPAPI() {
        return instance.hasPAPI;
    }

    public static boolean resourcePack$overrideUniform() {
        return instance.resourcePack$overrideUniform;
    }

    public static boolean resourcePack$protection$breakFormat() {
        return instance.resourcePack$protection$breakFormat;
    }

    public static int maxChainUpdate() {
        return instance.maxChainUpdate;
    }

    public static boolean removeInvalidFurniture() {
        return instance.removeInvalidFurniture;
    }

    public static Set<String> furnitureToRemove() {
        return instance.furnitureToRemove;
    }

    public static boolean forceUpdateLight() {
        return instance.forceUpdateLight;
    }

    public static boolean enableLightSystem() {
        return instance.enableLightSystem;
    }

    public static float packMinVersion() {
        return instance.packMinVersion;
    }

    public static float packMaxVersion() {
        return instance.packMaxVersion;
    }

    public static boolean enableSoundSystem() {
        return instance.enableSoundSystem;
    }

    public static boolean enableRecipeSystem() {
        return instance.enableRecipeSystem;
    }
}
