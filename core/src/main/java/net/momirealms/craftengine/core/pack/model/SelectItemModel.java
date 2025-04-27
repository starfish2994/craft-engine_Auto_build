package net.momirealms.craftengine.core.pack.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.model.select.SelectProperties;
import net.momirealms.craftengine.core.pack.model.select.SelectProperty;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.incendo.cloud.type.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectItemModel implements ItemModel {
    public static final Factory FACTORY = new Factory();
    private final SelectProperty property;
    private final Map<Either<String, List<String>>, ItemModel> whenMap;
    private final ItemModel fallBack;

    public SelectItemModel(@NotNull SelectProperty property,
                           @NotNull Map<Either<String, List<String>>, ItemModel> whenMap,
                           @Nullable ItemModel fallBack) {
        this.property = property;
        this.whenMap = whenMap;
        this.fallBack = fallBack;
    }

    public SelectProperty property() {
        return property;
    }

    public Map<Either<String, List<String>>, ItemModel> whenMap() {
        return whenMap;
    }

    public ItemModel fallBack() {
        return fallBack;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        property.accept(json);
        JsonArray array = new JsonArray();
        json.add("cases", array);
        for (Map.Entry<Either<String, List<String>>, ItemModel> entry : whenMap.entrySet()) {
            JsonObject item = new JsonObject();
            ItemModel itemModel = entry.getValue();
            item.add("model", itemModel.get());
            Either<String, List<String>> either = entry.getKey();
            if (either.primary().isPresent()) {
                item.addProperty("when", either.primary().get());
            } else {
                List<String> list = either.fallback().get();
                JsonArray whens = new JsonArray();
                for (String e : list) {
                    whens.add(e);
                }
                item.add("when", whens);
            }
            array.add(item);
        }
        if (fallBack != null) {
            json.add("fallback", fallBack.get());
        }
        return json;
    }

    @Override
    public Key type() {
        return ItemModels.SELECT;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> models = new ArrayList<>(4);
        if (fallBack != null) {
            models.addAll(fallBack.modelsToGenerate());
        }
        for (ItemModel itemModel : whenMap.values()) {
            models.addAll(itemModel.modelsToGenerate());
        }
        return models;
    }

    public static class Factory implements ItemModelFactory {

        @SuppressWarnings("unchecked")
        @Override
        public ItemModel create(Map<String, Object> arguments) {
            SelectProperty property = SelectProperties.fromMap(arguments);
            Map<String, Object> fallback = MiscUtils.castToMap(arguments.get("fallback"), true);

            Object casesObj = arguments.get("cases");
            if (casesObj instanceof List<?> list) {
                List<Map<String, Object>> cases = (List<Map<String, Object>>) list;
                if (!cases.isEmpty()) {
                    Map<Either<String, List<String>>, ItemModel> whenMap = new HashMap<>();
                    for (Map<String, Object> c : cases) {
                        Object when = c.get("when");
                        if (when == null) {
                            throw new LocalizedResourceConfigException("warning.config.item.model.select.case.lack_when", new NullPointerException("'when' should not be null"));
                        }
                        Either<String, List<String>> either;
                        if (when instanceof List<?> whenList) {
                            List<String> whens = new ArrayList<>(whenList.size());
                            for (Object o : whenList) {
                                whens.add(o.toString());
                            }
                            either = Either.ofFallback(whens);
                        } else {
                            either = Either.ofPrimary(when.toString());
                        }
                        Object model = c.get("model");
                        if (model == null) {
                            throw new LocalizedResourceConfigException("warning.config.item.model.select.case.lack_model", new NullPointerException("'model' should not be null"));
                        }
                        whenMap.put(either, ItemModels.fromMap(MiscUtils.castToMap(model, false)));
                    }
                    return new SelectItemModel(property, whenMap, fallback == null ? null : ItemModels.fromMap(fallback));
                } else {
                    throw new LocalizedResourceConfigException("warning.config.item.model.select.lack_cases", new NullPointerException("'cases' is required for the select model"));
                }
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.model.select.lack_cases", new NullPointerException("'cases' is required for the select model"));
            }
        }
    }
}
