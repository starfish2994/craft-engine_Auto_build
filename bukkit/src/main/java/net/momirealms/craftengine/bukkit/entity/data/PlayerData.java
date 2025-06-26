package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;

public class PlayerData<T> extends LivingEntityData<T> {
    public static final PlayerData<Float> PlayerAbsorption = new PlayerData<>(PlayerData.class, EntityDataValue.Serializers$FLOAT, 0.0f);
    public static final PlayerData<Integer> Score = new PlayerData<>(PlayerData.class, EntityDataValue.Serializers$INT, 0);
    public static final PlayerData<Byte> PlayerModeCustomisation = new PlayerData<>(PlayerData.class, EntityDataValue.Serializers$BYTE, (byte) 0);
    public static final PlayerData<Byte> PlayerMainHand = new PlayerData<>(PlayerData.class, EntityDataValue.Serializers$BYTE, (byte) 1);
    public static final PlayerData<Object> ShoulderLeft = new PlayerData<>(PlayerData.class, EntityDataValue.Serializers$COMPOUND_TAG, CoreReflections.instance$CompoundTag$Empty);
    public static final PlayerData<Object> ShoulderRight = new PlayerData<>(PlayerData.class, EntityDataValue.Serializers$COMPOUND_TAG, CoreReflections.instance$CompoundTag$Empty);

    public PlayerData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
