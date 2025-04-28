package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.nio.file.Path;
import java.util.Map;

public class InvertedPathMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final PathMatcher matcher;

    public InvertedPathMatcher(PathMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Key type() {
        return PathMatchers.INVERTED;
    }

    @Override
    public boolean test(Path path) {
        return !matcher.test(path);
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            Object inverted = arguments.get("term");
            if (inverted == null) {
                throw new LocalizedResourceConfigException("warning.config.conflict_matcher.inverted.missing_term", new NullPointerException("term should not be null for inverted"));
            }
            Map<String, Object> term = MiscUtils.castToMap(inverted, false);
            return new InvertedPathMatcher(PathMatchers.fromMap(term));
        }
    }
}
