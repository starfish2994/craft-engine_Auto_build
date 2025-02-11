package net.momirealms.craftengine.core.loot.provider;

import java.util.Map;

public interface NumberProviderFactory {

    NumberProvider create(Map<String, Object> arguments);
}
