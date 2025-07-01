package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class TrimBasedEquipment extends AbstractEquipment {
    public static final Factory FACTORY = new Factory();
    private final Key humanoid;
    private final Key humanoidLeggings;

    public TrimBasedEquipment(Key assetId, Key humanoid, Key humanoidLeggings) {
        super(assetId);
        this.humanoid = humanoid;
        this.humanoidLeggings = humanoidLeggings;
    }

    @Override
    public Key renderingMethod() {
        return Equipments.TRIM;
    }

    public Key humanoid() {
        return humanoid;
    }

    public Key humanoidLeggings() {
        return humanoidLeggings;
    }

    public static class Factory implements EquipmentFactory {
        @Override
        public Equipment create(Key id, Map<String, Object> args) {
            // todo node
            String humanoidId = ResourceConfigUtils.requireNonEmptyStringOrThrow(args.get("humanoid"), "");
            String humanoidLeggingsId = ResourceConfigUtils.requireNonEmptyStringOrThrow(args.get("humanoid-leggings"), "");
            // todo 验证resource location
            return new TrimBasedEquipment(id, Key.of(humanoidId), Key.of(humanoidLeggingsId));
        }
    }
}
