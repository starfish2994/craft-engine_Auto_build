package net.momirealms.craftengine.mod.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class CustomStreamCodec {
    public static Function<ItemStack, ItemStack> clientBoundDataProcessor;
    public static Function<ItemStack, ItemStack> serverBoundDataProcessor;

    public static @NotNull ItemStack s2c(@NotNull ItemStack itemStack) {
        if (clientBoundDataProcessor != null) {
            itemStack = clientBoundDataProcessor.apply(itemStack);
        }
        return itemStack;
    }

    public static @NotNull ItemStack c2s(@NotNull ItemStack itemStack) {
        if (serverBoundDataProcessor != null) {
            itemStack = serverBoundDataProcessor.apply(itemStack);
        }
        return itemStack;
    }
}