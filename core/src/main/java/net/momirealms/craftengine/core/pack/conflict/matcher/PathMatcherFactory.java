package net.momirealms.craftengine.core.pack.conflict.matcher;

import java.util.Map;

@FunctionalInterface
public interface PathMatcherFactory {

    PathMatcher create(Map<String, Object> arguments);
}
