package net.momirealms.craftengine.bukkit.entity;

import java.util.List;

public interface EntityData<T> {

    Object serializer();
    int id();
    T defaultValue();

    default Object createEntityDataIfNotDefaultValue(T value) {
        if (defaultValue().equals(value)) return null;
        return EntityDataValue.create(id(), serializer(), value);
    }

    default void addEntityDataIfNotDefaultValue(T value, List<Object> list) {
        if (!defaultValue().equals(value)) {
            list.add(EntityDataValue.create(id(), serializer(), value));
        }
    }

    default void addEntityData(T value, List<Object> list) {
        list.add(EntityDataValue.create(id(), serializer(), value));
    }

    static <T> EntityData<T> of(int id, Object serializer, T defaultValue) {
        return new SimpleEntityData<>(id, serializer, defaultValue);
    }
}
