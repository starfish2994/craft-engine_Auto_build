package net.momirealms.craftengine.bukkit.entity.data;

import java.util.List;
import java.util.Optional;

public class LivingEntityData<T> extends BaseEntityData<T> {
    public static final LivingEntityData<Byte> LivingEntityFlags = new LivingEntityData<>(8, EntityDataValue.Serializers$BYTE, (byte) 0);
    public static final LivingEntityData<Float> Health = new LivingEntityData<>(9, EntityDataValue.Serializers$FLOAT, 1.0f);
    public static final LivingEntityData<List<Object>> EffectParticles = new LivingEntityData<>(10, EntityDataValue.Serializers$PARTICLES, List.of());
    public static final LivingEntityData<Boolean> EffectAmbience = new LivingEntityData<>(11, EntityDataValue.Serializers$BOOLEAN, false);
    public static final LivingEntityData<Integer> ArrowCount = new LivingEntityData<>(12, EntityDataValue.Serializers$INT, 0);
    public static final LivingEntityData<Integer> StingerCount = new LivingEntityData<>(13, EntityDataValue.Serializers$INT, 0);
    public static final LivingEntityData<Optional<Object>> SleepingPos = new LivingEntityData<>(14, EntityDataValue.Serializers$OPTIONAL_BLOCK_POS, Optional.empty());

    public LivingEntityData(int id, Object serializer, T defaultValue) {
        super(id, serializer, defaultValue);
    }
}