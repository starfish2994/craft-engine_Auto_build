package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ExprEntityFurnitureID extends SimplePropertyExpression<Object, String> {

    public static void register() {
        register(ExprEntityFurnitureID.class, String.class, "furniture id", "entities");
    }

    @Override
    public @Nullable String convert(Object object) {
        if (object instanceof Entity entity && CraftEngineFurniture.isFurniture(entity))
            return Objects.requireNonNull(CraftEngineFurniture.getLoadedFurnitureByBaseEntity(entity)).id().toString();
        return null;
    }

    @Override
    protected String getPropertyName() {
        return "furniture id";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
