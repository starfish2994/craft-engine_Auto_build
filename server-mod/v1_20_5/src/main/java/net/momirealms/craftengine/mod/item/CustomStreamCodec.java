package net.momirealms.craftengine.mod.item;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class CustomStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, ItemStack> {
    public static Function<ItemStack, ItemStack> clientBoundDataProcessor;
    public static Function<ItemStack, ItemStack> serverBoundDataProcessor;

    private final StreamCodec<RegistryFriendlyByteBuf, ItemStack> original;

    public CustomStreamCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStack> original) {
        this.original = Objects.requireNonNull(original);
    }

    @Override
    public @NotNull ItemStack decode(@NotNull RegistryFriendlyByteBuf buffer) {
        ItemStack itemStack = this.original.decode(buffer);
        if (!itemStack.isEmpty()) {
            if (serverBoundDataProcessor != null) {
                itemStack = serverBoundDataProcessor.apply(itemStack);
            }
        }
        return itemStack;
    }

    @Override
    public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ItemStack value) {
        if (!value.isEmpty()) {
            if (clientBoundDataProcessor != null) {
                value = clientBoundDataProcessor.apply(value);
            }
        }
        this.original.encode(buffer, value);
    }
}