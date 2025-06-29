package net.momirealms.craftengine.core.item.setting;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.misc.EquipmentLayerType;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ItemEquipment {
    private final EquipmentData data;
    private final EnumMap<EquipmentLayerType, List<Layer>> layers;

    public ItemEquipment(EquipmentData data) {
        this.data = data;
        this.layers = new EnumMap<>(EquipmentLayerType.class);
    }

    public void addLayer(EquipmentLayerType layerType, List<Layer> layer) {
        this.layers.put(layerType, layer);
    }

    public EnumMap<EquipmentLayerType, List<Layer>> layers() {
        return layers;
    }

    public EquipmentData data() {
        return data;
    }

    public record Layer(String texture, DyeableData data, boolean usePlayerTexture) implements Supplier<JsonObject> {

        @NotNull
        public static List<Layer> fromConfig(Object obj) {
            switch (obj) {
                case String texture -> {
                    return List.of(new Layer(texture, null, false));
                }
                case Map<?, ?> map -> {
                    Map<String, Object> data = MiscUtils.castToMap(map, false);
                    String texture = data.get("texture").toString();
                    return List.of(new Layer(texture,
                            DyeableData.fromObj(data.get("dyeable")),
                            ResourceConfigUtils.getAsBoolean(data.getOrDefault("use-player-texture", false), "use-player-texture")
                    ));
                }
                case List<?> list -> {
                    List<Layer> layers = new ArrayList<>();
                    for (Object inner : list) {
                        layers.addAll(fromConfig(inner));
                    }
                    return layers;
                }
                case null, default -> {
                    return List.of();
                }
            }
        }

        @Override
        public JsonObject get() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("texture", texture);
            if (this.data != null) {
                jsonObject.add("dyeable", this.data.get());
            }
            if (this.usePlayerTexture) {
                jsonObject.addProperty("use_player_texture", true);
            }
            return jsonObject;
        }

        public record DyeableData(@Nullable Integer colorWhenUndyed) implements Supplier<JsonObject> {

            public static DyeableData fromObj(Object obj) {
                if (obj instanceof Map<?,?> map) {
                    Map<String, Object> data = MiscUtils.castToMap(map, false);
                    if (data.containsKey("color-when-undyed")) {
                        return new DyeableData(ResourceConfigUtils.getAsInt(data.get("color-when-undyed"), "color-when-undyed"));
                    }
                }
                return new DyeableData(null);
            }

            @Override
            public JsonObject get() {
                JsonObject dyeData = new JsonObject();
                if (this.colorWhenUndyed != null) {
                    dyeData.addProperty("color_when_undyed", this.colorWhenUndyed);
                }
                return dyeData;
            }
        }
    }
}
