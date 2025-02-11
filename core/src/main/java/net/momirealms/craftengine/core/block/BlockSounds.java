package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class BlockSounds {
    public static final Key EMPTY_SOUND = Key.of("minecraft:intentionally_empty");
    public static final BlockSounds EMPTY = new BlockSounds(EMPTY_SOUND, EMPTY_SOUND, EMPTY_SOUND, EMPTY_SOUND, EMPTY_SOUND);

    private final Key breakSound;
    private final Key stepSound;
    private final Key placeSound;
    private final Key hitSound;
    private final Key fallSound;

    public BlockSounds(Key breakSound, Key stepSound, Key placeSound, Key hitSound, Key fallSound) {
        this.breakSound = breakSound;
        this.stepSound = stepSound;
        this.placeSound = placeSound;
        this.hitSound = hitSound;
        this.fallSound = fallSound;
    }

    public static BlockSounds fromMap(Map<String, Object> map) {
        if (map == null) return EMPTY;
        return new BlockSounds(
                Key.of(map.getOrDefault("break", "minecraft:intentionally_empty").toString()),
                Key.of(map.getOrDefault("step", "minecraft:intentionally_empty").toString()),
                Key.of(map.getOrDefault("place", "minecraft:intentionally_empty").toString()),
                Key.of(map.getOrDefault("hit", "minecraft:intentionally_empty").toString()),
                Key.of(map.getOrDefault("fall", "minecraft:intentionally_empty").toString())
        );
    }

    public Key breakSound() {
        return breakSound;
    }

    public Key stepSound() {
        return stepSound;
    }

    public Key placeSound() {
        return placeSound;
    }

    public Key hitSound() {
        return hitSound;
    }

    public Key fallSound() {
        return fallSound;
    }
}
