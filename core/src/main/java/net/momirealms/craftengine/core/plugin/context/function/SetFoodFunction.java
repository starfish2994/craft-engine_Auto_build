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

public class SetFoodFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final NumberProvider count;
    private final boolean add;

    public SetFoodFunction(NumberProvider count, boolean add, List<Condition<CTX>> predicates) {
        super(predicates);
        this.count = count;
        this.add = add;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<Player> optionalPlayer = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        optionalPlayer.ifPresent(player -> player.setFoodLevel(this.add ? player.foodLevel() + this.count.getInt(ctx) : this.count.getInt(ctx)));
    }

    @Override
    public Key type() {
        return CommonFunctions.SET_FOOD;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            Object value = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("food"), "warning.config.function.set_food.missing_food");
            boolean add = (boolean) arguments.getOrDefault("add", false);
            return new SetFoodFunction<>(NumberProviders.fromObject(value), add, getPredicates(arguments));
        }
    }
}
