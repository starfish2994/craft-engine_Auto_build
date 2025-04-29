package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;

public class ParentPathPrefixMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final String prefix;

    public ParentPathPrefixMatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean test(Path path) {
        Path parent = path.getParent();
        if (parent == null) return false;
        String pathStr = parent.toString().replace("\\", "/");
        return pathStr.startsWith(this.prefix);
    }

    @Override
    public Key type() {
        return PathMatchers.PARENT_PATH_PREFIX;
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            String prefix = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("prefix"), () -> new LocalizedException("warning.config.conflict_matcher.parent_prefix.missing_prefix"));
            return new ParentPathPrefixMatcher(prefix);
        }
    }
}
