package net.momirealms.craftEngineFabricMod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.momirealms.craftEngineFabricMod.client.network.CraftEnginePayload;
import net.momirealms.craftEngineFabricMod.client.util.BlockUtils;

public class CraftEngineFabricModClient implements ClientModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(CraftEnginePayload.ID, CraftEnginePayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CraftEnginePayload.ID, (payload, context) -> {});
        Registries.BLOCK.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);
            if (id.getNamespace().equals(CraftEngineFabricModClient.MOD_ID)) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());
                if (id.getPath().contains("leaves")) {
                    BlockUtils.registerColor(block);
                }
            }
        });
    }
}
