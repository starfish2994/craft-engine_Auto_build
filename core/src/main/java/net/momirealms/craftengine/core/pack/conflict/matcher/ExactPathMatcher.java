package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;

public class ExactPathMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final String path;

    public ExactPathMatcher(String path) {
        this.path = path;
    }

    @Override
    public boolean test(Path path) {
        String pathStr = path.toString().replace("\\", "/");
        return pathStr.equals(this.path);
    }

    @Override
    public Key type() {
        return PathMatchers.EXACT;
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            String path = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("path"), () -> new LocalizedException("warning.config.conflict_matcher.exact.missing_path"));
            return new ExactPathMatcher(path);
        }
    }
}
