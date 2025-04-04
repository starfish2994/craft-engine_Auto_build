package net.momirealms.craftengine.core.pack.model.generation;

import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractModelGenerator implements ModelGenerator {
    protected final CraftEngine plugin;
    protected final Map<Key, ModelGeneration> modelsToGenerate = new HashMap<>();

    public AbstractModelGenerator(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public Collection<ModelGeneration> modelsToGenerate() {
        return this.modelsToGenerate.values();
    }

    @Override
    public void clearModelsToGenerate() {
        this.modelsToGenerate.clear();
    }

    public void prepareModelGeneration(Path path, Key id, ModelGeneration model) {
        ModelGeneration conflict = this.modelsToGenerate.get(model.path());
        if (conflict != null) {
            if (conflict.equals(model)) {
                return;
            }
            TranslationManager.instance().log("warning.config.model.generation.conflict", path.toString(), id.toString(), model.path().toString());
            return;
        }
        for (Map.Entry<String, String> texture : model.texturesOverride().entrySet()) {
            if (!ResourceLocation.isValid(texture.getValue())) {
                TranslationManager.instance().log("warning.config.model.generation.texture.invalid_resource_location", path.toString(), id.toString(), texture.getKey(), texture.getValue());
            }
        }
        this.modelsToGenerate.put(model.path(), model);
    }
}
