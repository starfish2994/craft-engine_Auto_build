package net.momirealms.craftengine.bukkit.entity.data;

public class AnimalData<T> extends AgeableMobData<T> {

    public AnimalData(int id, Object serializer, T defaultValue) {
        super(id, serializer, defaultValue);
    }
}
