package net.momirealms.craftengine.core.font.emoji;

import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;

public interface EmojiManager extends Reloadable, ConfigSectionParser {
    String[] CONFIG_SECTION_NAME = new String[] {"emoji", "emojis"};

    default String[] sectionId() {
        return CONFIG_SECTION_NAME;
    }

    default int loadingSequence() {
        return LoadingSequence.EMOJI;
    }
}
