package net.momirealms.craftengine.bukkit.entity.data;

public class MobData<T> extends LivingEntityData<T> {
    public static final MobData<Byte> MobFlags = new MobData<>(15, EntityDataValue.Serializers$BYTE, (byte) 0);

    public MobData(int id, Object serializer, T defaultValue) {
        super(id, serializer, defaultValue);
    }
}