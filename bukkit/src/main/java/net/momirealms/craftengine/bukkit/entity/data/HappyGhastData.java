package net.momirealms.craftengine.bukkit.entity.data;

public class HappyGhastData<T> extends AnimalData<T> {
    public static final HappyGhastData<Boolean> IsLeashHolder = new HappyGhastData<>(17, EntityDataValue.Serializers$BOOLEAN, false);
    public static final BaseEntityData<Boolean> StaysStill = new HappyGhastData<>(18, EntityDataValue.Serializers$BOOLEAN, false);

    public HappyGhastData(int id, Object serializer, T defaultValue) {
        super(id, serializer, defaultValue);
    }
}
