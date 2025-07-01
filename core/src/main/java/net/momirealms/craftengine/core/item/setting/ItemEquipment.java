package net.momirealms.craftengine.core.item.setting;

import net.momirealms.craftengine.core.item.equipment.ComponentBasedEquipment;
import net.momirealms.craftengine.core.pack.misc.EquipmentLayerType;

import java.util.EnumMap;
import java.util.List;

public class ItemEquipment {
    private final EquipmentData data;
    private final ComponentBasedEquipment equipment;

    public ItemEquipment(EquipmentData data, ComponentBasedEquipment equipment) {
        this.data = data;
        this.equipment = equipment;
    }

    public void addLayer(EquipmentLayerType layerType, List<ComponentBasedEquipment.Layer> layer) {
        this.equipment.addLayer(layerType, layer);
    }

    public EnumMap<EquipmentLayerType, List<ComponentBasedEquipment.Layer>> layers() {
        return this.equipment.layers();
    }

    public EquipmentData data() {
        return data;
    }
}
