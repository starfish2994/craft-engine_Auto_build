package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AllOfPathMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final List<PathMatcher> matchers;

    public AllOfPathMatcher(List<PathMatcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public Key type() {
        return PathMatchers.ALL_OF;
    }

    @Override
    public boolean test(Path path) {
        for (PathMatcher matcher : matchers) {
            if (!matcher.test(path)) {
                return false;
            }
        }
        return true;
    }

    public static class Factory implements PathMatcherFactory {

        @SuppressWarnings("unchecked")
        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            Object termsObj = arguments.get("terms");
            if (termsObj instanceof List<?> list) {
                List<Map<String, Object>> terms = (List<Map<String, Object>>) list;
                return new AllOfPathMatcher(PathMatchers.fromMapList(terms));
            } else if (termsObj instanceof Map<?, ?>) {
                Map<String, Object> terms = MiscUtils.castToMap(termsObj, false);
                return new AllOfPathMatcher(PathMatchers.fromMapList(List.of(terms)));
            } else {
                throw new LocalizedResourceConfigException("warning.config.conflict_matcher.all_of.missing_terms", new NullPointerException("terms should not be null for all_of"));
            }
        }
    }
}
