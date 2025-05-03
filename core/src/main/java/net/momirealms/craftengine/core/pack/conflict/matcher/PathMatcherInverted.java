package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.condition.InvertedCondition;

import java.util.Map;

public class PathMatcherInverted extends InvertedCondition<PathContext> implements PathMatcher {
    public static final Factory FACTORY = new Factory();

    public PathMatcherInverted(PathMatcher condition) {
        super(condition);
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            Object inverted = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("term"), () -> new LocalizedException("warning.config.conflict_matcher.inverted.missing_term"));
            Map<String, Object> term = MiscUtils.castToMap(inverted, false);
            return new PathMatcherInverted(PathMatchers.fromMap(term));
        }
    }
}
