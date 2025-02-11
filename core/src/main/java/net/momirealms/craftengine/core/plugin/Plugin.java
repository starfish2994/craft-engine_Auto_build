package net.momirealms.craftengine.core.plugin;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.craftengine.core.block.BlockManager;
import net.momirealms.craftengine.core.entity.furniture.FurnitureManager;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.font.FontManager;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.plugin.classpath.ClassPathAppender;
import net.momirealms.craftengine.core.plugin.command.sender.SenderFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.plugin.config.template.TemplateManager;
import net.momirealms.craftengine.core.plugin.dependency.DependencyManager;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.logger.PluginLogger;
import net.momirealms.craftengine.core.plugin.network.NetworkManager;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerAdapter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface Plugin extends Reloadable {

    InputStream resourceStream(String filePath);

    PluginLogger logger();

    ClassPathAppender classPathAppender();

    File dataFolderFile();

    Path dataFolderPath();

    DependencyManager dependencyManager();

    SchedulerAdapter<?> scheduler();

    void saveResource(String filePath);

    YamlDocument loadYamlConfig(String filePath, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings);

    YamlDocument loadYamlData(File file);

    String pluginVersion();

    String serverVersion();

    <T> ItemManager<T> itemManager();

    BlockManager blockManager();

    NetworkManager networkManager();

    FontManager fontManager();

    ConfigManager configManager();

    TranslationManager translationManager();

    TemplateManager templateManager();

    FurnitureManager furnitureManager();

    SenderFactory<? extends Plugin, ?> senderFactory();

    boolean isPluginEnabled(String plugin);

    String parse(Player player, String text);

    Path resolveConfig(String filePath);
}
