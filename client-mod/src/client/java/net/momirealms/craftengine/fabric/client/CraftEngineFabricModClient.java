package net.momirealms.craftengine.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.biome.FoliageColors;
import net.momirealms.craftengine.fabric.client.config.ModConfig;
import net.momirealms.craftengine.fabric.client.network.CraftEnginePayload;

import java.nio.charset.StandardCharsets;

public class CraftEngineFabricModClient implements ClientModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(CraftEnginePayload.ID, CraftEnginePayload.CODEC);
        Registries.BLOCK.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);
            if (id.getNamespace().equals(CraftEngineFabricModClient.MOD_ID)) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());
                if (id.getPath().contains("leaves")) {
                    registerColor(block);
                }
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!ModConfig.enableNetwork) {
                ClientPlayNetworking.unregisterGlobalReceiver(CraftEnginePayload.ID.id());
                return;
            }
            ClientPlayNetworking.registerGlobalReceiver(CraftEnginePayload.ID, (payload, context) -> {
                byte[] data = payload.data();
                String decoded = new String(data, StandardCharsets.UTF_8);
                if (decoded.startsWith("cp:")) {
                    int blockRegistrySize = Integer.parseInt(decoded.substring(3));
                    if (Block.STATE_IDS.size() != blockRegistrySize) {
                        client.disconnect(new DisconnectedScreen(
                                client.currentScreen,
                                Text.translatable("disconnect.craftengine.title"),
                                Text.translatable("disconnect.craftengine.block_registry_mismatch", Block.STATE_IDS.size(), blockRegistrySize))
                        );
                    }
                }
            });
        });
    }

    public static void registerColor(Block block) {
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> {
                    if (world != null && pos != null) {
                        return BiomeColors.getFoliageColor(world, pos);
                    }
                    return FoliageColors.DEFAULT;
                },
                block
        );
    }
}
