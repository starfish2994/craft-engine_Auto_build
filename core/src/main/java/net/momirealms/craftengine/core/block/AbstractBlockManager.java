package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.pack.model.generation.AbstractModelGenerator;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.*;

public abstract class AbstractBlockManager extends AbstractModelGenerator implements BlockManager {
    // CraftEngine objects
    protected final Map<Key, CustomBlock> byId = new HashMap<>();
    // Cached command suggestions
    protected final List<Suggestion> cachedSuggestions = new ArrayList<>();
    // Cached Namespace
    protected final Set<String> namespacesInUse = new HashSet<>();

    public AbstractBlockManager(CraftEngine plugin) {
        super(plugin);
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
    public void unload() {
        super.clearModelsToGenerate();
        this.cachedSuggestions.clear();
        this.byId.clear();
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    public Set<String> namespacesInUse() {
        return Collections.unmodifiableSet(this.namespacesInUse);
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
}
