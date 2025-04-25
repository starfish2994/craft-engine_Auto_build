package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExprCustomBlockProperty extends PropertyExpression<ImmutableBlockState, String> {

    public static void register() {
        Skript.registerExpression(
                ExprCustomBlockProperty.class,
                String.class,
                ExpressionType.PROPERTY,
                "[the] custom block %strings% propert(y|ies) of %customblockstates%",
                "%customblockstates%'[s] custom block %strings% propert(y|ies)"
        );
    }

    private Expression<String> properties;

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String[] get(Event event, ImmutableBlockState[] source) {
        String[] props = this.properties.getArray(event);
        List<String> results = new ArrayList<>();

        for (ImmutableBlockState state : source) {
            for (String propName : props) {
                Property<?> property = state.owner().value().getProperty(propName);
                if (property != null) {
                    results.add(state.get(property).toString());
                }
            }
        }
        return results.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        if (matchedPattern == 0) {
            properties = (Expression<String>) exprs[0];
            setExpr((Expression<? extends ImmutableBlockState>) exprs[1]);
        } else {
            properties = (Expression<String>) exprs[1];
            setExpr((Expression<? extends ImmutableBlockState>) exprs[0]);
        }
        return true;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "custom block state " + getExpr().toString(event, debug) +
                "'s " + properties.toString(event, debug) + " property";
    }
}