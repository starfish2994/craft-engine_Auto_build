package net.momirealms.craftengine.core.pack;

import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;

public interface PackManager extends Manageable {

    void loadResources(boolean recipe);

    @NotNull
    Collection<Pack> loadedPacks();

    boolean registerConfigSectionParser(ConfigSectionParser parser);

    default void registerConfigSectionParsers(ConfigSectionParser[] parsers) {
        for (ConfigSectionParser parser : parsers) {
            registerConfigSectionParser(parser);
        }
    }

    boolean unregisterConfigSectionParser(String id);

    default void unregisterConfigSectionParser(ConfigSectionParser parser) {
        for (String id : parser.sectionId()) {
            unregisterConfigSectionParser(id);
        }
    }

    void generateResourcePack();

    Path resourcePackPath();

    ResourcePackHost resourcePackHost();

    void uploadResourcePack();
}
