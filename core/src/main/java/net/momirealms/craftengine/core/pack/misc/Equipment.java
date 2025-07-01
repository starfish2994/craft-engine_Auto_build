package net.momirealms.craftengine.core.pack.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.equipment.ComponentBasedEquipment;
import net.momirealms.craftengine.core.item.setting.ItemEquipment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Equipment implements Supplier<JsonObject> {
    private final EnumMap<EquipmentLayerType, List<ComponentBasedEquipment.Layer>> layers;

    public Equipment() {
        this.layers = new EnumMap<>(EquipmentLayerType.class);
    }

    public void addAll(ItemEquipment equipment) {
        for (Map.Entry<EquipmentLayerType, List<ComponentBasedEquipment.Layer>> entry : equipment.layers().entrySet()) {
            List<ComponentBasedEquipment.Layer> layers = entry.getValue();
            List<ComponentBasedEquipment.Layer> previous = this.layers.put(entry.getKey(), layers);
            if (previous != null && !previous.equals(layers)) {
                // todo 是否异常
            }
        }
    }

    @Override
    public JsonObject get() {
        JsonObject jsonObject = new JsonObject();
        JsonObject layersJson = new JsonObject();
        jsonObject.add("layers", layersJson);
        for (Map.Entry<EquipmentLayerType, List<ComponentBasedEquipment.Layer>> entry : layers.entrySet()) {
            EquipmentLayerType type = entry.getKey();
            List<ComponentBasedEquipment.Layer> layerList = entry.getValue();
            setLayers(layersJson, layerList, type.id());
        }
        return jsonObject;
    }

    private void setLayers(JsonObject layersJson, List<ComponentBasedEquipment.Layer> layers, String key) {
        if (layers == null || layers.isEmpty()) return;
        JsonArray layersArray = new JsonArray();
        for (ComponentBasedEquipment.Layer layer : layers) {
            layersArray.add(layer.get());
        }
        layersJson.add(key, layersArray);
    }
}
