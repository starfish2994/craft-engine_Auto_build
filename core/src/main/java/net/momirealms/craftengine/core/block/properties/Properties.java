package net.momirealms.craftengine.core.block.properties;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public class Properties {
    public static final Key BOOLEAN = Key.of("craftengine:boolean");
    public static final Key INT = Key.of("craftengine:int");
    public static final Key STRING = Key.of("craftengine:string");
    public static final Key AXIS = Key.of("craftengine:axis");
    public static final Key HORIZONTAL_DIRECTION = Key.of("craftengine:4-direction");
    public static final Key DIRECTION = Key.of("craftengine:6-direction");

    static {
        register(BOOLEAN, BooleanProperty.FACTORY);
        register(INT, IntegerProperty.FACTORY);
        register(STRING, StringProperty.FACTORY);
        register(AXIS, new EnumProperty.Factory<>(Direction.Axis.class));
        register(DIRECTION, new EnumProperty.Factory<>(Direction.class));
        register(HORIZONTAL_DIRECTION, new EnumProperty.Factory<>(HorizontalDirection.class));
    }

    public static void register(Key key, PropertyFactory factory) {
        Holder.Reference<PropertyFactory> holder = ((WritableRegistry<PropertyFactory>) BuiltInRegistries.PROPERTY_FACTORY).registerForHolder(new ResourceKey<>(Registries.PROPERTY_FACTORY.location(), key));
        holder.bindValue(factory);
    }

    public static Property<?> fromMap(String name, Map<String, Object> map) {
        Object type = map.get("type");
        if (type == null) {
            throw new LocalizedResourceConfigException("warning.config.block.state.property.missing_type", new NullPointerException("'type' cannot be null for block state property"), name);
        }
        Key key = Key.withDefaultNamespace(type.toString(), "craftengine");
        PropertyFactory factory = BuiltInRegistries.PROPERTY_FACTORY.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.block.state.property.invalid_type", new IllegalArgumentException("Unknown property type: " + type), key.toString(), name);
        }
        return factory.create(name, map);
    }
}
