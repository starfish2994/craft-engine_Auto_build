package net.momirealms.craftengine.core.block;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.pack.model.generation.AbstractModelGenerator;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractBlockManager extends AbstractModelGenerator implements BlockManager {
    // CraftEngine objects
    protected final Map<Key, CustomBlock> byId = new HashMap<>();
    // Cached command suggestions
    protected final List<Suggestion> cachedSuggestions = new ArrayList<>();
    // Cached Namespace
    protected final Set<String> namespacesInUse = new HashSet<>();
    // for mod, real block id -> state models
    protected final Map<Key, JsonElement> modBlockStates = new HashMap<>();
    // A temporary map that stores the model path of a certain vanilla block state
    protected final Map<Integer, JsonElement> tempVanillaBlockStateModels = new Int2ObjectOpenHashMap<>();
    // A temporary map used to detect whether the same block state corresponds to multiple models.
    protected final Map<Integer, Key> tempRegistryIdConflictMap = new Int2ObjectOpenHashMap<>();
    // A temporary map that converts the custom block registered on the server to the vanilla block ID.
    protected final Map<Integer, Integer> tempBlockAppearanceConvertor = new Int2IntOpenHashMap();
    // Used to store override information of json files
    protected final Map<Key, Map<String, JsonElement>> blockStateOverrides = new HashMap<>();
    // a reverted mapper
    protected final Map<Integer, List<Integer>> appearanceToRealState = new Int2ObjectOpenHashMap<>();
    // client side block tags
    protected Map<Integer, List<String>> clientBoundTags = Map.of();
    protected Map<Integer, List<String>> previousClientBoundTags = Map.of();

    protected AbstractBlockManager(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    public void unload() {
        super.clearModelsToGenerate();
        this.clearCache();
        this.cachedSuggestions.clear();
        this.blockStateOverrides.clear();
        this.modBlockStates.clear();
        this.byId.clear();
        this.previousClientBoundTags = this.clientBoundTags;
        this.clientBoundTags = new HashMap<>();
        this.appearanceToRealState.clear();
    }

    @Override
    public void delayedLoad() {
        this.initSuggestions();
        this.clearCache();
        this.resendTags();
    }

    @Override
    public Map<Key, CustomBlock> blocks() {
        return Collections.unmodifiableMap(this.byId);
    }

    @Override
    public Optional<CustomBlock> blockById(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    @Override
    public void addBlock(Key id, CustomBlock customBlock) {
        this.byId.put(id, customBlock);
        // generate mod assets
        if (Config.generateModAssets()) {
            for (ImmutableBlockState state : customBlock.variantProvider().states()) {
                this.modBlockStates.put(getBlockOwnerId(state.customBlockState()), this.tempVanillaBlockStateModels.get(state.vanillaBlockState().registryId()));
            }
        }
    }

    @Override
    public Map<Key, JsonElement> modBlockStates() {
        return Collections.unmodifiableMap(this.modBlockStates);
    }

    @Override
    public Map<Key, Map<String, JsonElement>> blockOverrides() {
        return Collections.unmodifiableMap(this.blockStateOverrides);
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    public Set<String> namespacesInUse() {
        return Collections.unmodifiableSet(this.namespacesInUse);
    }

    protected void clearCache() {
        this.tempRegistryIdConflictMap.clear();
        this.tempBlockAppearanceConvertor.clear();
        this.tempVanillaBlockStateModels.clear();
    }

    protected void initSuggestions() {
        this.cachedSuggestions.clear();
        this.namespacesInUse.clear();
        Set<String> states = new HashSet<>();
        for (CustomBlock block : this.byId.values()) {
            states.add(block.id().toString());
            this.namespacesInUse.add(block.id().namespace());
            for (ImmutableBlockState state : block.variantProvider().states()) {
                states.add(state.toString());
            }
        }
        for (String state : states) {
            this.cachedSuggestions.add(Suggestion.suggestion(state));
        }
    }

    @NotNull
    public List<Integer> appearanceToRealStates(int appearanceStateId) {
        return Optional.ofNullable(this.appearanceToRealState.get(appearanceStateId)).orElse(List.of());
    }

    protected abstract void resendTags();

    protected abstract boolean isVanillaBlock(Key id);

    protected abstract int getBlockRegistryId(Key id);

    public class BlockParser implements ConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"blocks", "block"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.BLOCK;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            if (isVanillaBlock(id)) {
                parseVanillaBlock(pack, path, id, section);
            } else {
                // check duplicated config
                if (AbstractBlockManager.this.byId.containsKey(id)) {
                    throw new LocalizedResourceConfigException("warning.config.block.duplicate");
                }
                parseCustomBlock(pack, path, id, section);
            }
        }

        private void parseVanillaBlock(Pack pack, Path path, Key id, Map<String, Object> section) {
            Map<String, Object> settings = MiscUtils.castToMap(section.get("settings"), true);
            if (settings != null) {
                Object clientBoundTags = settings.get("client-bound-tags");
                if (clientBoundTags instanceof List<?> list) {
                    List<String> clientSideTags = MiscUtils.getAsStringList(list).stream().filter(ResourceLocation::isValid).toList();
                    AbstractBlockManager.this.clientBoundTags.put(getBlockRegistryId(id), clientSideTags);
                }
            }
        }

        private void parseCustomBlock(Pack pack, Path path, Key id, Map<String, Object> section) {
            // 获取方块设置
            BlockSettings settings = BlockSettings.fromMap(id, MiscUtils.castToMap(section.get("settings"), true));
            //
        }
    }
}
