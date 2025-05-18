package net.momirealms.craftengine.core.pack.model;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public class LegacyItemModel {
    private final List<ModelGeneration> modelsToGenerate;
    private final String path;
    private final List<LegacyOverridesModel> overrides;

    public LegacyItemModel(String path, List<LegacyOverridesModel> overrides, List<ModelGeneration> modelsToGenerate) {
        this.modelsToGenerate = modelsToGenerate;
        this.path = path;
        this.overrides = overrides;
    }

    public List<ModelGeneration> modelsToGenerate() {
        return modelsToGenerate;
    }

    public List<LegacyOverridesModel> overrides() {
        return overrides;
    }

    public String path() {
        return path;
    }

    public static LegacyItemModel fromMap(Map<String, Object> legacyModel) {
        String legacyModelPath = ResourceConfigUtils.requireNonEmptyStringOrThrow(legacyModel.get("path"), "warning.config.item.legacy_model.missing_path");
        Map<String, Object> generation = MiscUtils.castToMap(legacyModel.get("generation"), true);
        ModelGeneration modelGeneration = null;
        if (generation != null) {
            modelGeneration = ModelGeneration.of(Key.of(legacyModelPath), generation);
        }
        LegacyOverridesModel legacyOverridesModel = new LegacyOverridesModel();
    }
}
