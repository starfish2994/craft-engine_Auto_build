package net.momirealms.craftengine.core.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum DamageCause {
    BLOCK_EXPLOSION,
    CAMPFIRE,
    CONTACT,
    CRAMMING,
    CUSTOM,
    DROWNING,
    DRYOUT,
    ENTITY_ATTACK,
    ENTITY_EXPLOSION,
    ENTITY_SWEEP_ATTACK,
    FALL,
    FALLING_BLOCK,
    FIRE,
    FIRE_TICK,
    FLY_INTO_WALL,
    FREEZE,
    HOT_FLOOR,
    KILL,
    LAVA,
    LIGHTNING,
    MAGIC,
    MELTING,
    POISON,
    PROJECTILE,
    SONIC_BOOM,
    STARVATION,
    SUFFOCATION,
    SUICIDE,
    THORNS,
    VOID,
    WITHER,
    WORLD_BORDER,
    @Deprecated
    @SuppressWarnings("all")
    DRAGON_BREATH;

    public static final Map<String, DamageCause> BY_NAME = new HashMap<>();

    static {
        for (DamageCause cause : values()) {
            BY_NAME.put(cause.name().toLowerCase(Locale.ROOT), cause);
            BY_NAME.put(cause.name(), cause);
        }
    }

    public static DamageCause byName(String name) {
        return Optional.ofNullable(BY_NAME.get(name)).orElseThrow(() -> new IllegalArgumentException("Unknown damage cause: " + name));
    }

}
