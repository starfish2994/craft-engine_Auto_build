package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public class PathContainsMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final String path;

    public PathContainsMatcher(String path) {
        this.path = path;
    }

    @Override
    public boolean test(Path path) {
        String pathStr = path.toString().replace("\\", "/");
        return pathStr.contains(this.path);
    }

    @Override
    public Key type() {
        return PathMatchers.CONTAINS;
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            String path = (String) arguments.get("path");
            if (path == null) {
                throw new LocalizedResourceConfigException("warning.config.conflict_matcher.contains.lack_path", new NullPointerException("path should not be null"));
            }
            return new PathContainsMatcher(path);
        }
    }
}
