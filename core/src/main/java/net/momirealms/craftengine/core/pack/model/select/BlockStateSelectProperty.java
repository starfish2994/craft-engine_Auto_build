package net.momirealms.craftengine.core.pack.model.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;
import java.util.Objects;

public class BlockStateSelectProperty implements SelectProperty {
    public static final Factory FACTORY = new Factory();
    private final String blockStateProperty;

    public BlockStateSelectProperty(String blockStateProperty) {
        this.blockStateProperty = blockStateProperty;
    }

    @Override
    public Key type() {
        return SelectProperties.BLOCK_STATE;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", type().toString());
        jsonObject.addProperty("block_state_property", blockStateProperty);
    }

    public static class Factory implements SelectPropertyFactory {

        @Override
        public SelectProperty create(Map<String, Object> arguments) {
            Object property = arguments.get("block-state-property");
            if (property == null) {
                throw new LocalizedResourceConfigException("warning.config.item.model.select.block_state.missing_property", new NullPointerException("block-state-property should not be null"));
            }
            String blockStateProperty = property.toString();
            return new BlockStateSelectProperty(blockStateProperty);
        }
    }
}
