package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Enchantment;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.condition.LootCondition;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApplyBonusCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final Key enchantment;
    private final Formula formula;

    public ApplyBonusCountFunction(List<LootCondition> predicates, Key enchantment, Formula formula) {
        super(predicates);
        this.enchantment = enchantment;
        this.formula = formula;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        Optional<Item<?>> itemInHand = context.getOptionalParameter(LootParameters.TOOL);
        int level = itemInHand.map(value -> value.getEnchantment(this.enchantment).map(Enchantment::level).orElse(0)).orElse(0);
        int newCount = formula.apply(item.count(), level);
        item.count(newCount);
        return item;
    }

    @Override
    public Key type() {
        return LootFunctions.APPLY_BONUS;
    }

    public static class Factory<T> implements LootFunctionFactory<T> {

        @Override
        public LootFunction<T> create(Map<String, Object> arguments) {
            return null;
        }
    }

    public interface Formula {
        int apply(int initialCount, int enchantmentLevel);

        Key type();
    }
}
