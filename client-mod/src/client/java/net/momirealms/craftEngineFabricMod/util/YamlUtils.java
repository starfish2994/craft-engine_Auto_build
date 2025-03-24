package net.momirealms.craftEngineFabricMod.util;

import com.mojang.brigadier.StringReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlUtils {
    public static final String CONFIG_DIR = "config/craft-engine-fabric-mod/";
    private static final Yaml yaml = new Yaml();
    private static final RegistryWrapper<Block> registryWrapper = BuiltinRegistries.createWrapperLookup().getOrThrow(RegistryKeys.BLOCK);

    public static <T> T loadConfig(Path filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return yaml.load(inputStream);
        }
    }

    public static void ensureConfigFile(String fileName) throws IOException {
        Path configDir = Path.of(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        Path targetPath = configDir.resolve(fileName);
        if (Files.exists(targetPath)) return;
        String resourcePath = "assets/craft-engine-fabric-mod/config/" + fileName;
        try (InputStream inputStream = YamlUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Default config file not found: " + resourcePath);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    public static Map<Identifier, Integer> loadMappingsAndAdditionalBlocks() throws IOException {
        Path mappingPath = Path.of(CONFIG_DIR + "mappings.yml");
        Path additionalYamlPath = Path.of(CONFIG_DIR + "additional-real-blocks.yml");
        Map<String, String> blockStateMappings = loadConfig(mappingPath);
        validateBlockStateMappings(blockStateMappings);
        Map<Identifier, Integer> blockTypeCounter = new LinkedHashMap<>();
        Map<Integer, Integer> appearanceMapper = new HashMap<>();
        for (Map.Entry<String, String> entry : blockStateMappings.entrySet()) {
            processBlockStateMapping(entry, appearanceMapper, blockTypeCounter);
        }
        Map<String, Integer> additionalYaml = loadConfig(additionalYamlPath);
        return buildRegisteredRealBlockSlots(blockTypeCounter, additionalYaml);
    }


    private static void validateBlockStateMappings(Map<String, String> blockStateMappings) {
        Map<String, String> temp = new HashMap<>(blockStateMappings);
        for (Map.Entry<String, String> entry : temp.entrySet()) {
            String state = entry.getValue();
            blockStateMappings.remove(state);
        }
    }

    private static void processBlockStateMapping(
            Map.Entry<String, String> entry,
            Map<Integer, Integer> stateIdMapper,
            Map<Identifier, Integer> blockUsageCounter
    ) {
        final BlockState sourceState = createBlockData(entry.getKey());
        final BlockState targetState = createBlockData(entry.getValue());

        if (sourceState == null || targetState == null) {
            return;
        }

        final int sourceStateId = Block.STATE_IDS.getRawId(sourceState);
        final int targetStateId = Block.STATE_IDS.getRawId(targetState);

        if (stateIdMapper.putIfAbsent(sourceStateId, targetStateId) == null) {
            final Block sourceBlock = sourceState.getBlock();
            final Identifier blockId = Registries.BLOCK.getId(sourceBlock);
            blockUsageCounter.merge(blockId, 1, Integer::sum);
        }
    }

    public static BlockState createBlockData(String blockState) {
        try {
            StringReader reader = new StringReader(blockState);
            BlockArgumentParser.BlockResult arg = BlockArgumentParser.block(registryWrapper, reader, true);
            return arg.blockState();
        } catch (Exception e) {
            return null;
        }
    }

    private static LinkedHashMap<Identifier, Integer> buildRegisteredRealBlockSlots(Map<Identifier, Integer> counter, Map<String, Integer> additionalYaml) {
        LinkedHashMap<Identifier, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Integer> entry : counter.entrySet()) {
            String id = entry.getKey().toString();
            Integer additionalStates = additionalYaml.get(id);
            int internalIds = entry.getValue() + (additionalStates != null ? additionalStates : 0);
            map.put(entry.getKey(), internalIds);
        }
        return map;
    }
}
