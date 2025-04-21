package net.momirealms.craftengine.core.advancement;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;

public interface AdvancementManager extends Manageable {

    ConfigSectionParser parser();
}
