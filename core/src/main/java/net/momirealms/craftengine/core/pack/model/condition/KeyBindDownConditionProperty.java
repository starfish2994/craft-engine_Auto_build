package net.momirealms.craftengine.core.pack.model.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class KeyBindDownConditionProperty implements ConditionProperty {
    public static final Factory FACTORY = new Factory();
    private final String keybind;

    public KeyBindDownConditionProperty(String keybind) {
        this.keybind = keybind;
    }

    @Override
    public Key type() {
        return ConditionProperties.KEYBIND_DOWN;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", type().toString());
        jsonObject.addProperty("keybind", keybind);
    }

    public static class Factory implements ConditionPropertyFactory {

        @Override
        public ConditionProperty create(Map<String, Object> arguments) {
            String keybindObj = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("keybind"), "warning.config.item.model.condition.keybind.missing");
            return new KeyBindDownConditionProperty(keybindObj);
        }
    }
}
