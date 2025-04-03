package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.util.Key;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.*;

public abstract class AbstractFurnitureManager implements FurnitureManager {
    protected final Map<Key, CustomFurniture> byId = new HashMap<>();
    // Cached command suggestions
    private final List<Suggestion> cachedSuggestions = new ArrayList<>();

    @Override
    public void delayedLoad() {
        this.initSuggestions();
    }

    @Override
    public void initSuggestions() {
        this.cachedSuggestions.clear();
        for (Key key : this.byId.keySet()) {
            this.cachedSuggestions.add(Suggestion.suggestion(key.toString()));
        }
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    @Override
    public Optional<CustomFurniture> furnitureById(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    @Override
    public void unload() {
        this.byId.clear();
    }
}
