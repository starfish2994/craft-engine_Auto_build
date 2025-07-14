package net.momirealms.craftengine.bukkit.item.recipe;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public record PredicateChoice(Predicate<ItemStack> predicate) implements RecipeChoice {

    @Override
    public @NotNull RecipeChoice clone() {
        try {
            return (PredicateChoice) super.clone();
        } catch (final CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        throw new UnsupportedOperationException("PredicateChoice doesn't support getItemStack()");
    }

    @Override
    public boolean test(@NotNull ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }
}
