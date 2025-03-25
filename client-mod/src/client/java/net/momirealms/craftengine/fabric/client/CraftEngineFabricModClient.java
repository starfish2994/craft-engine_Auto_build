package net.momirealms.craftengine.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.FoliageColors;
import net.momirealms.craftengine.fabric.client.config.ModConfig;
import net.momirealms.craftengine.fabric.client.network.CraftEnginePayload;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CraftEngineFabricModClient implements ClientModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(CraftEnginePayload.ID, CraftEnginePayload.CODEC);
        initChannel(MinecraftClient.getInstance().getNetworkHandler());
        registerRenderLayer();
        ClientPlayConnectionEvents.INIT.register((handler, client) -> initChannel(handler));
    }

    public static void registerRenderLayer() {
        Registries.BLOCK.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);
            if (id.getNamespace().equals(CraftEngineFabricModClient.MOD_ID)) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());
                if (id.getPath().contains("leaves")) {
                    registerColor(block);
                }
            }
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

    private static void initChannel(ClientPlayNetworkHandler handler) {
        if (ModConfig.enableNetwork) {
            registerChannel(handler);
        } else {
            ClientPlayNetworking.unregisterGlobalReceiver(CraftEnginePayload.ID.id());
        }
    }

    private static void registerChannel(ClientPlayNetworkHandler handler) {
        ClientPlayNetworking.registerGlobalReceiver(CraftEnginePayload.ID, (payload, context) -> {
            byte[] data = payload.data();
            String decoded = new String(data, StandardCharsets.UTF_8);
            if (decoded.startsWith("cp:")) {
                int blockRegistrySize = Integer.parseInt(decoded.substring(3));
                if (Block.STATE_IDS.size() != blockRegistrySize) {
                    handler.getConnection().disconnect(
                            new DisconnectionInfo(
                                    Text.translatable("disconnect.craftengine.block_registry_mismatch", Block.STATE_IDS.size(), blockRegistrySize),
                                    Optional.of(FabricLoader.getInstance().getConfigDir().resolve("craft-engine-fabric-mod/mappings.yml")),
                                    Optional.of(URI.create("https://github.com/Xiao-MoMi/craft-engine"))
                            )
                    );
                }
            }
        });
    }
}
