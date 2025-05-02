package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class PathMatcherFilename implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final String name;

    public PathMatcherFilename(String name) {
        this.name = name;
    }

    @Override
    public boolean test(PathContext path) {
        String fileName = String.valueOf(path.path().getFileName());
        return fileName.equals(name);
    }

    @Override
    public Key type() {
        return PathMatchers.FILENAME;
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            String name = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("name"), () -> new LocalizedException("warning.config.conflict_matcher.filename.missing_name"));
            return new PathMatcherFilename(name);
        }
    }
}
