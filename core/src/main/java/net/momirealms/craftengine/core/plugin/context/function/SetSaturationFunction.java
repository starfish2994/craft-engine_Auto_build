package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SetSaturationFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final NumberProvider count;
    private final boolean add;

    public SetSaturationFunction(NumberProvider count, boolean add, List<Condition<CTX>> predicates) {
        super(predicates);
        this.count = count;
        this.add = add;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<Player> optionalPlayer = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        optionalPlayer.ifPresent(player -> player.setSaturation(this.add ? player.saturation() + this.count.getFloat(ctx) : this.count.getFloat(ctx)));
    }

    @Override
    public Key type() {
        return CommonFunctions.SET_SATURATION;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            Object value = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("saturation"), "warning.config.function.set_saturation.missing_saturation");
            boolean add = (boolean) arguments.getOrDefault("add", false);
            return new SetSaturationFunction<>(NumberProviders.fromObject(value), add, getPredicates(arguments));
        }
    }
}
