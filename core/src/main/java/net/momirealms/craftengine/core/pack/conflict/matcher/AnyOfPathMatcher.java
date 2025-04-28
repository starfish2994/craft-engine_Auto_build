package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AnyOfPathMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final List<PathMatcher> matchers;

    public AnyOfPathMatcher(List<PathMatcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public Key type() {
        return PathMatchers.ANY_OF;
    }

    @Override
    public boolean test(Path path) {
        for (PathMatcher matcher : matchers) {
            if (matcher.test(path)) {
                return true;
            }
        }
        return false;
    }

    public static class Factory implements PathMatcherFactory {

        @SuppressWarnings("unchecked")
        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            Object termsObj = arguments.get("terms");
            if (termsObj instanceof List<?> list) {
                List<Map<String, Object>> terms = (List<Map<String, Object>>) list;
                return new AnyOfPathMatcher(PathMatchers.fromMapList(terms));
            } else if (termsObj instanceof Map<?, ?>) {
                Map<String, Object> terms = MiscUtils.castToMap(termsObj, false);
                return new AnyOfPathMatcher(PathMatchers.fromMapList(List.of(terms)));
            } else {
                throw new LocalizedResourceConfigException("warning.config.conflict_matcher.any_of.missing_terms");
            }
        }
    }
}
