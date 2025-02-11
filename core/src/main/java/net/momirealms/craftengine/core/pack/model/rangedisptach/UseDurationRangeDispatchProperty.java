package net.momirealms.craftengine.core.pack.model.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class UseDurationRangeDispatchProperty implements RangeDispatchProperty {
    public static final Factory FACTORY = new Factory();
    private final boolean remaining;

    public UseDurationRangeDispatchProperty(boolean remaining) {
        this.remaining = remaining;
    }

    @Override
    public Key type() {
        return RangeDispatchProperties.USE_DURATION;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", type().toString());
        if (remaining) {
            jsonObject.addProperty("remaining", true);
        }
    }

    public static class Factory implements RangeDispatchPropertyFactory {

        @Override
        public RangeDispatchProperty create(Map<String, Object> arguments) {
            boolean remaining = (boolean) arguments.getOrDefault("remaining", false);
            return new UseDurationRangeDispatchProperty(remaining);
        }
    }
}
