package net.momirealms.craftengine.core.pack.model.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

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
            Object keybindObj = arguments.get("keybind");
            if (keybindObj == null) {
                throw new LocalizedResourceConfigException("warning.config.item.model.condition.keybind.missing");
            }
            String keybind = keybindObj.toString();
            return new KeyBindDownConditionProperty(keybind);
        }
    }
}
