package net.momirealms.craftEngineFabricMod;

import net.fabricmc.api.ModInitializer;
import net.momirealms.craftEngineFabricMod.util.RegisterBlocks;
import net.momirealms.craftEngineFabricMod.util.YamlUtils;

import java.util.HashMap;
import java.util.Map;

public class CraftEngineFabricMod implements ModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitialize() {
        Map<String, String> mappings = YamlUtils.loadConfig();
        Map<String, Integer> blockCount = new HashMap<>();
        mappings.keySet().forEach(name -> {
            String blockName = YamlUtils.split(name);
            if (blockName != null) {
                if (blockCount.containsKey(blockName)) {
                    blockCount.put(blockName, blockCount.get(blockName) + 1);
                } else {
                    blockCount.put(blockName, 0);
                }
                RegisterBlocks.register(blockName + "_" + blockCount.get(blockName));
            }
        });
        mappings.clear();
        blockCount.clear();
    }
}
