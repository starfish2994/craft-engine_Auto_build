package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NumberProviders {
    public static final Key FIXED = Key.of("craftengine:constant");
    public static final Key UNIFORM = Key.of("craftengine:uniform");

    static {
        register(FIXED, FixedNumberProvider.FACTORY);
        register(UNIFORM, UniformNumberProvider.FACTORY);
    }

    public static void register(Key key, NumberProviderFactory factory) {
        Holder.Reference<NumberProviderFactory> holder = ((WritableRegistry<NumberProviderFactory>) BuiltInRegistries.NUMBER_PROVIDER_FACTORY)
                .registerForHolder(new ResourceKey<>(Registries.NUMBER_PROVIDER_FACTORY.location(), key));
        holder.bindValue(factory);
    }

    public static List<NumberProvider> fromMapList(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) return List.of();
        List<NumberProvider> functions = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            functions.add(fromMap(map));
        }
        return functions;
    }

    public static NumberProvider fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.loot_table.number.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        NumberProviderFactory factory = BuiltInRegistries.NUMBER_PROVIDER_FACTORY.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.loot_table.number.invalid_type", type);
        }
        return factory.create(map);
    }

    @SuppressWarnings("unchecked")
    public static NumberProvider fromObject(Object object) {
        if (object == null) {
            throw new NullPointerException("number argument is null");
        }
        if (object instanceof Number number) {
            return new FixedNumberProvider(number.floatValue());
        } else if (object instanceof String string) {
            if (string.contains("~")) {
                int first = string.indexOf('~');
                int second = string.indexOf('~', first + 1);
                if (second == -1) {
                    try {
                        float min = Float.parseFloat(string.substring(0, first));
                        float max = Float.parseFloat(string.substring(first + 1));
                        return new UniformNumberProvider(min, max);
                    } catch (NumberFormatException e) {
                        throw e;
                    }
                } else {
                    throw new IllegalArgumentException("Illegal number format: " + string);
                }
            } else {
                return new FixedNumberProvider(Float.parseFloat(string));
            }
        } else if (object instanceof Map<?,?> map) {
            return fromMap((Map<String, Object>) map);
        }
        throw new IllegalArgumentException("Can't convert " + object + " to " + NumberProvider.class.getSimpleName());
    }
}
