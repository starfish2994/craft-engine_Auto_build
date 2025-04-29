package net.momirealms.craftengine.core.pack.model.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class BannerSpecialModel implements SpecialModel {
    public static final Factory FACTORY = new Factory();
    private final String color;

    public BannerSpecialModel(String color) {
        this.color = color;
    }

    @Override
    public Key type() {
        return SpecialModels.BANNER;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        json.addProperty("color", color);
        return json;
    }

    public static class Factory implements SpecialModelFactory {

        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            String color = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("color"), "warning.config.item.model.special.banner.missing_color");
            return new BannerSpecialModel(color);
        }
    }
}
