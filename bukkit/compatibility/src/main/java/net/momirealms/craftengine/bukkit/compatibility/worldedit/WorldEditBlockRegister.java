package net.momirealms.craftengine.bukkit.compatibility.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitBlockRegistry;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.momirealms.craftengine.core.block.AbstractBlockManager;
import net.momirealms.craftengine.core.block.BlockStateParser;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class WorldEditBlockRegister {
    private final Field field$BlockType$blockMaterial;
    private final AbstractBlockManager manager;
    private final Set<String> cachedSuggestions = new HashSet<>();

    public WorldEditBlockRegister(AbstractBlockManager manager) {
        field$BlockType$blockMaterial = ReflectionUtils.getDeclaredField(BlockType.class, "blockMaterial");
        this.manager = manager;
    }

    public void enable() {
        CEBlockParser blockParser = new CEBlockParser(WorldEdit.getInstance());
        WorldEdit.getInstance().getBlockFactory().register(blockParser);
    }

    public void load() {
        Collection<?> cachedSuggestions = manager.cachedSuggestions();
        for (Object o : cachedSuggestions) {
            this.cachedSuggestions.add(o.toString());
        }
    }

    public void unload() {
        cachedSuggestions.clear();
    }

    public void register(Key id) throws ReflectiveOperationException {
        BlockType blockType = new BlockType(id.toString(), blockState -> blockState);
        field$BlockType$blockMaterial.set(blockType, LazyReference.from(() -> new BukkitBlockRegistry.BukkitBlockMaterial(null, Material.STONE)));
        BlockType.REGISTRY.register(id.toString(), blockType);
    }

    private class CEBlockParser extends InputParser<BaseBlock> {

        protected CEBlockParser(WorldEdit worldEdit) {
            super(worldEdit);
        }

        @Override
        public Stream<String> getSuggestions(String input) {
            Set<String> namespacesInUse = manager.namespacesInUse();

            if (input.isEmpty() || input.equals(":")) {
                return namespacesInUse.stream().map(namespace -> namespace + ":");
            }

            if (input.startsWith(":")) {
                String term = input.substring(1).toLowerCase();
                return cachedSuggestions.stream().filter(s -> s.toLowerCase().contains(term));
            }

            if (!input.contains(":")) {
                String lowerSearch = input.toLowerCase();
                return Stream.concat(
                        namespacesInUse.stream().filter(n -> n.startsWith(lowerSearch)).map(n -> n + ":"),
                        cachedSuggestions.stream().filter(s -> s.toLowerCase().startsWith(lowerSearch))
                );
            }
            return cachedSuggestions.stream().filter(s -> s.toLowerCase().startsWith(input.toLowerCase()));
        }

        @Override
        public BaseBlock parseFromInput(String input, ParserContext context) {
            int colonIndex = input.indexOf(':');
            if (colonIndex == -1) return null;

            Set<String> namespacesInUse = manager.namespacesInUse();
            String namespace = input.substring(0, colonIndex);
            if (!namespacesInUse.contains(namespace)) return null;

            ImmutableBlockState state = BlockStateParser.deserialize(input);
            if (state == null) return null;

            try {
                String id = state.customBlockState().handle().toString();
                int first = id.indexOf('{');
                int last = id.indexOf('}');
                if (first != -1 && last != -1 && last > first) {
                    String blockId = id.substring(first + 1, last);
                    BlockType blockType = BlockTypes.get(blockId);
                    if (blockType == null) {
                        return null;
                    }
                    return blockType.getDefaultState().toBaseBlock();
                } else {
                    throw new IllegalArgumentException("Invalid block ID format: " + id);
                }
            } catch (NullPointerException e) {
                return null;
            }
        }
    }
}
