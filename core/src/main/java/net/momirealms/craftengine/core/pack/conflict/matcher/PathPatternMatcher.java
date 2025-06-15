package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public class PathPatternMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final Pattern pattern;

    public PathPatternMatcher(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public PathPatternMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean test(Path path) {
        String pathStr = path.toString().replace("\\", "/");
        return this.pattern.matcher(pathStr).matches();
    }

    @Override
    public Key type() {
        return PathMatchers.PATTERN;
    }

    public Pattern pattern() {
        return pattern;
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            String pattern = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("pattern"), () -> new LocalizedException("warning.config.conflict_matcher.pattern.missing_pattern"));
            return new PathPatternMatcher(pattern);
        }
    }
}
