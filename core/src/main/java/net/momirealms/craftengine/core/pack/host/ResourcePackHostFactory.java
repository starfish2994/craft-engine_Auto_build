package net.momirealms.craftengine.core.pack.host;

import java.util.Map;

public interface ResourcePackHostFactory {

    ResourcePackHost create(Map<String, Object> arguments);
}
