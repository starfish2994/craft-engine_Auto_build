package net.momirealms.craftengine.core.sound;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface SoundManager extends Manageable {

    boolean isVanillaSoundEvent(Key key);

    ConfigSectionParser[] parsers();

    Map<Key, SoundEvent> sounds();
}
