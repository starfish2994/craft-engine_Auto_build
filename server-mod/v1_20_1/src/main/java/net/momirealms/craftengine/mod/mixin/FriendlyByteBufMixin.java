package net.momirealms.craftengine.mod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.mod.item.CustomStreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FriendlyByteBuf.class)
public class FriendlyByteBufMixin {

    @ModifyVariable(
            method = "a(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/PacketDataSerializer;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack modifyWriteItemParam(ItemStack stack) {
        return stack.isEmpty() ? stack : CustomStreamCodec.s2c(stack);
    }

    @ModifyReturnValue(
            method = "r()Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private ItemStack modifyReadItemStack(ItemStack original) {
        return original.isEmpty() ? original : CustomStreamCodec.c2s(original);
    }
}
