package net.momirealms.craftengine.mod.mixin;

import net.minecraft.world.level.block.Blocks;
import net.momirealms.craftengine.mod.block.CustomBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Blocks.class)
public abstract class BlocksMixin {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onBlocksInit(CallbackInfo ci) {
        CustomBlocks.register();
    }
}
