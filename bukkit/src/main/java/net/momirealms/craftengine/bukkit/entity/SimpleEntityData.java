package net.momirealms.craftengine.bukkit.entity;

public class SimpleEntityData<T> implements EntityData<T> {

    private final int id;
    private final Object serializer;
    private final T defaultValue;

    public SimpleEntityData(int id, Object serializer, T defaultValue) {
        this.id = id;
        this.serializer = serializer;
        this.defaultValue = defaultValue;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public Object serializer() {
        return serializer;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }
}
