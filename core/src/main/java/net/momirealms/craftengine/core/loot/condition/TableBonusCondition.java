package net.momirealms.craftengine.core.loot.condition;

import net.momirealms.craftengine.core.item.Enchantment;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TableBonusCondition implements LootCondition {
    public static final Factory FACTORY = new Factory();
    private final Key enchantmentType;
    private final List<Float> values;

    public TableBonusCondition(Key enchantmentType, List<Float> values) {
        this.enchantmentType = enchantmentType;
        this.values = values;
    }

    @Override
    public Key type() {
        return LootConditions.TABLE_BONUS;
    }

    @Override
    public boolean test(LootContext lootContext) {
        Optional<Item<?>> item = lootContext.getOptionalParameter(LootParameters.TOOL);
        int level = item.map(value -> value.getEnchantment(this.enchantmentType).map(Enchantment::level).orElse(0)).orElse(0);
        float f = this.values.get(Math.min(level, this.values.size() - 1));
        return lootContext.randomSource().nextFloat() < f;
    }

    public static class Factory implements LootConditionFactory {
        @Override
        public LootCondition create(Map<String, Object> arguments) {
            Object enchantmentObj = arguments.get("enchantment");
            if (enchantmentObj == null) {
                throw new LocalizedResourceConfigException("warning.config.loot_table.condition.table_bonus.missing_enchantment");
            }
            Key enchantmentType = Key.of(enchantmentObj.toString());
            Object chances = arguments.get("chances");
            if (chances != null) {
                if (chances instanceof Number number) {
                    return new TableBonusCondition(enchantmentType, List.of(number.floatValue()));
                } else if (chances instanceof List<?> list) {
                    List<Float> values = new ArrayList<>(list.size());
                    for (Object o : list) {
                        values.add(ResourceConfigUtils.getAsFloat(o, "chances"));
                    }
                    return new TableBonusCondition(enchantmentType, values);
                }
            }
            throw new LocalizedResourceConfigException("warning.config.loot_table.condition.table_bonus.missing_chances");
        }
    }
}
