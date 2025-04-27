package net.momirealms.craftengine.core.pack.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompositeItemModel implements ItemModel {
    public static final Factory FACTORY = new Factory();
    private final List<ItemModel> models;

    public CompositeItemModel(List<ItemModel> models) {
        this.models = models;
    }

    public List<ItemModel> models() {
        return models;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        JsonArray array = new JsonArray();
        for (ItemModel model : models) {
            array.add(model.get());
        }
        json.add("models", array);
        return json;
    }

    @Override
    public Key type() {
        return ItemModels.COMPOSITE;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> models = new ArrayList<>(4);
        for (ItemModel model : this.models) {
            models.addAll(model.modelsToGenerate());
        }
        return models;
    }

    public static class Factory implements ItemModelFactory {

        @SuppressWarnings("unchecked")
        @Override
        public ItemModel create(Map<String, Object> arguments) {
            Object m = arguments.get("models");
            if (m instanceof List<?> list) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) list;
                if (models.isEmpty()) {
                    throw new LocalizedResourceConfigException("warning.config.item.model.composite.lack_models", new IllegalArgumentException("'models' list should not be empty for 'minecraft:composite'"));
                }
                List<ItemModel> modelList = new ArrayList<>();
                for (Map<String, Object> model : models) {
                    modelList.add(ItemModels.fromMap(model));
                }
                return new CompositeItemModel(modelList);
            } else if (m instanceof Map<?, ?> map) {
                return new CompositeItemModel(List.of(ItemModels.fromMap(MiscUtils.castToMap(map, false))));
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.model.composite.lack_models", new NullPointerException("'models' argument is required for 'minecraft:composite'"));
            }
        }
    }
}
