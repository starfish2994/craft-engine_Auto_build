package net.momirealms.craftengine.core.pack.model.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class HeadSpecialModel implements SpecialModel {
    public static final Factory FACTORY = new Factory();
    private final String kind;
    private final String texture;
    private final int animation;

    public HeadSpecialModel(String kind, String texture, int animation) {
        this.kind = kind;
        this.texture = texture;
        this.animation = animation;
    }

    @Override
    public Key type() {
        return SpecialModels.HEAD;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        json.addProperty("kind", kind);
        json.addProperty("texture", texture);
        json.addProperty("animation", animation);
        return json;
    }

    public static class Factory implements SpecialModelFactory {

        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            Object kind = arguments.get("kind");
            if (kind == null) {
                throw new LocalizedResourceConfigException("warning.config.item.model.special.head.missing_kind");
            }
            Object texture = arguments.get("texture");
            if (texture == null) {
                throw new LocalizedResourceConfigException("warning.config.item.model.special.head.missing_texture");
            }
            int animation = ResourceConfigUtils.getAsInt(arguments.getOrDefault("animation", 0), "animation");
            return new HeadSpecialModel(kind.toString(), texture.toString(), animation);
        }
    }
}
