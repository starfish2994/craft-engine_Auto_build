package net.momirealms.craftengine.core.pack.model.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class BedSpecialModel implements SpecialModel {
    public static final Factory FACTORY = new Factory();
    private final String texture;

    public BedSpecialModel(String texture) {
        this.texture = texture;
    }

    @Override
    public Key type() {
        return SpecialModels.BED;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        json.addProperty("texture", texture);
        return json;
    }

    public static class Factory implements SpecialModelFactory {

        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            String color = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("texture"), "warning.config.item.model.special.bed.missing_texture");
            return new BedSpecialModel(color);
        }
    }
}
