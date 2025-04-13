package net.momirealms.craftengine.mod.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.momirealms.craftengine.mod.item.CustomStreamCodec;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow(remap = false)
    @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC;
    @Shadow(remap = false)
    @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> OPTIONAL_LIST_STREAM_CODEC;

    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> ORIGINAL_OPTIONAL_STREAM_CODEC;

    @Inject(
            method = "<clinit>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/ItemStack;OPTIONAL_STREAM_CODEC:Lnet/minecraft/network/codec/StreamCodec;",
                    opcode = Opcodes.PUTSTATIC,
                    shift = At.Shift.AFTER
            )
    )
    private static void captureOriginalAfterAssignment(CallbackInfo ci) {
        ORIGINAL_OPTIONAL_STREAM_CODEC = OPTIONAL_STREAM_CODEC;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void replaceStreamCodec(CallbackInfo ci) {
        OPTIONAL_STREAM_CODEC = new CustomStreamCodec(ORIGINAL_OPTIONAL_STREAM_CODEC);
        OPTIONAL_LIST_STREAM_CODEC = OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    }
}
