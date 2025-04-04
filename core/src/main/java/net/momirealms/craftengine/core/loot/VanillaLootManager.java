package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;

public interface VanillaLootManager extends Manageable {

    ConfigSectionParser parser();

    Optional<VanillaLoot> getBlockLoot(int blockState);

    Optional<VanillaLoot> getEntityLoot(Key entity);
}
