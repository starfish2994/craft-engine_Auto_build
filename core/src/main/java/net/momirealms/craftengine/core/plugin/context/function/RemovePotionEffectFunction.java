package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.*;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public class RemovePotionEffectFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final Key potionEffectType;

    public RemovePotionEffectFunction(Key potionEffectType, PlayerSelector<CTX> selector, List<Condition<CTX>> predicates) {
        super(predicates);
        this.potionEffectType = potionEffectType;
        this.selector = selector;
    }

    @Override
    public void runInternal(CTX ctx) {
        if (this.selector == null) {
            ctx.getOptionalParameter(DirectContextParameters.PLAYER).ifPresent(it -> {
                it.removePotionEffect(this.potionEffectType);
            });
        } else {
            for (Player target : this.selector.get(ctx)) {
                target.removePotionEffect(this.potionEffectType);
            }
        }
    }

    @Override
    public Key type() {
        return CommonFunctions.REMOVE_POTION_EFFECT;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            Key effectType = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("potion-effect"), "warning.config.function.remove_potion_effect.missing_potion_effect"));
            return new RemovePotionEffectFunction<>(effectType, PlayerSelectors.fromObject(arguments.get("target"), conditionFactory()), getPredicates(arguments));
        }
    }
}
