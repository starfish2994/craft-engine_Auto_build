package net.momirealms.craftengine.bukkit.compatibility.skript.condition;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CondIsCustomItem extends Condition {

    public static void register() {
        Skript.registerCondition(CondIsCustomItem.class,
                "%itemstacks% (is|are) custom item(s)",
                "%itemstacks% (is|are)(n't| not) custom item(s)");
    }

    private Expression<ItemStack> items;

    @Override
    public boolean check(Event event) {
        return items.check(event, CraftEngineItems::isCustomItem, isNegated());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, items, "itemstack");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        items = (Expression<ItemStack>) expressions[0];
        setNegated(matchedPattern > 1);
        return true;
    }
}
