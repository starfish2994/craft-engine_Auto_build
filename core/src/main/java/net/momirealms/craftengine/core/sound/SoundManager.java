package net.momirealms.craftengine.core.sound;

import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface SoundManager extends Reloadable {

    ConfigSectionParser[] parsers();

    Map<Key, SoundEvent> sounds();
}
