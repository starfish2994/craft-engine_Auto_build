package net.momirealms.craftengine.core.pack.model.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;

public class ShulkerBoxSpecialModel implements SpecialModel {
    public static final Factory FACTORY = new Factory();
    private final String texture;
    private final float openness;
    private final Direction orientation;

    public ShulkerBoxSpecialModel(String texture, float openness, Direction orientation) {
        this.texture = texture;
        this.openness = openness;
        this.orientation = orientation;
    }

    @Override
    public Key type() {
        return SpecialModels.SHULKER_BOX;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        json.addProperty("texture", texture);
        json.addProperty("openness", openness);
        json.addProperty("orientation", orientation.name().toLowerCase(Locale.ENGLISH));
        return json;
    }

    public static class Factory implements SpecialModelFactory {

        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            float openness = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("openness", 0), "openness");
            Object texture = arguments.get("texture");
            if (texture == null) {
                throw new LocalizedResourceConfigException("warning.config.item.model.special.shulker_box.missing_texture");
            }
            Direction orientation = Direction.valueOf(arguments.getOrDefault("orientation", "down").toString().toUpperCase(Locale.ENGLISH));
            if (openness > 1 || openness < 0) {
                throw new LocalizedResourceConfigException("warning.config.item.model.special.shulker_box.invalid_openness", String.valueOf(openness));
            }
            return new ShulkerBoxSpecialModel(texture.toString(), openness, orientation);
        }
    }
}
