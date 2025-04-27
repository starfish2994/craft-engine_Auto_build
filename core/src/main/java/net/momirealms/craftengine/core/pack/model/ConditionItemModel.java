package net.momirealms.craftengine.core.pack.model;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.condition.ConditionProperties;
import net.momirealms.craftengine.core.pack.model.condition.ConditionProperty;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConditionItemModel implements ItemModel {
    public static final Factory FACTORY = new Factory();
    private final ConditionProperty property;
    private final ItemModel onTrue;
    private final ItemModel onFalse;

    public ConditionItemModel(ConditionProperty property, ItemModel onTrue, ItemModel onFalse) {
        this.property = property;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    public ConditionProperty property() {
        return property;
    }

    public ItemModel onTrue() {
        return onTrue;
    }

    public ItemModel onFalse() {
        return onFalse;
    }

    @Override
    public Key type() {
        return ItemModels.CONDITION;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> models = new ArrayList<>(4);
        models.addAll(onTrue.modelsToGenerate());
        models.addAll(onFalse.modelsToGenerate());
        return models;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        json.add("on_true", onTrue.get());
        json.add("on_false", onFalse.get());
        property.accept(json);
        return json;
    }

    public static class Factory implements ItemModelFactory {

        @Override
        public ItemModel create(Map<String, Object> arguments) {
            ConditionProperty property = ConditionProperties.fromMap(arguments);
            ItemModel onTrue;
            if (arguments.get("on-true") instanceof Map<?,?> map1) {
                onTrue = ItemModels.fromMap(MiscUtils.castToMap(map1, false));
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.model.condition.lack_on_true", new NullPointerException("'on-true' is required for condition"));
            }
            ItemModel onFalse;
            if (arguments.get("on-false") instanceof Map<?,?> map2) {
                onFalse = ItemModels.fromMap(MiscUtils.castToMap(map2, false));
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.model.condition.lack_on_false", new NullPointerException("'on-false' is required for condition"));
            }
            return new ConditionItemModel(property, onTrue, onFalse);
        }
    }
}
