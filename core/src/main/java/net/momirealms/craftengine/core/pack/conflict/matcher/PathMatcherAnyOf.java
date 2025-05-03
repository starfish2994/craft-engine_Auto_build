package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.AnyOfCondition;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.List;
import java.util.Map;

public class PathMatcherAnyOf extends AnyOfCondition<PathContext> implements PathMatcher {
    public static final Factory FACTORY = new Factory();

    public PathMatcherAnyOf(List<? extends Condition<PathContext>> conditions) {
        super(conditions);
    }

    public static class Factory implements PathMatcherFactory {

        @SuppressWarnings("unchecked")
        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            Object termsObj = arguments.get("terms");
            if (termsObj instanceof List<?> list) {
                List<Map<String, Object>> terms = (List<Map<String, Object>>) list;
                return new PathMatcherAnyOf(PathMatchers.fromMapList(terms));
            } else if (termsObj instanceof Map<?, ?>) {
                Map<String, Object> terms = MiscUtils.castToMap(termsObj, false);
                return new PathMatcherAnyOf(PathMatchers.fromMapList(List.of(terms)));
            } else {
                throw new LocalizedException("warning.config.conflict_matcher.any_of.missing_terms");
            }
        }
    }
}
