package net.momirealms.craftengine.bukkit.entity.data;

public class AgeableMobData<T> extends PathfinderMobData<T> {
    public static final MobData<Boolean> Baby = new AgeableMobData<>(16, EntityDataValue.Serializers$BOOLEAN, false);

    public AgeableMobData(int id, Object serializer, T defaultValue) {
        super(id, serializer, defaultValue);
    }
}
