package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.util.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageCauseUtils {
    
    private DamageCauseUtils() {}

    @SuppressWarnings("deprecation")
    public static EntityDamageEvent.DamageCause toBukkit(DamageCause cause) {
        return switch (cause) {
            case BLOCK_EXPLOSION -> EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;
            case CAMPFIRE -> EntityDamageEvent.DamageCause.CAMPFIRE;
            case CONTACT -> EntityDamageEvent.DamageCause.CONTACT;
            case CRAMMING -> EntityDamageEvent.DamageCause.CRAMMING;
            case CUSTOM -> EntityDamageEvent.DamageCause.CUSTOM;
            case DROWNING -> EntityDamageEvent.DamageCause.DROWNING;
            case DRYOUT -> EntityDamageEvent.DamageCause.DRYOUT;
            case ENTITY_ATTACK -> EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            case ENTITY_EXPLOSION -> EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
            case ENTITY_SWEEP_ATTACK -> EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
            case FALL -> EntityDamageEvent.DamageCause.FALL;
            case FALLING_BLOCK -> EntityDamageEvent.DamageCause.FALLING_BLOCK;
            case FIRE -> EntityDamageEvent.DamageCause.FIRE;
            case FIRE_TICK -> EntityDamageEvent.DamageCause.FIRE_TICK;
            case FLY_INTO_WALL -> EntityDamageEvent.DamageCause.FLY_INTO_WALL;
            case FREEZE -> EntityDamageEvent.DamageCause.FREEZE;
            case HOT_FLOOR -> EntityDamageEvent.DamageCause.HOT_FLOOR;
            case KILL -> EntityDamageEvent.DamageCause.KILL;
            case LAVA -> EntityDamageEvent.DamageCause.LAVA;
            case LIGHTNING -> EntityDamageEvent.DamageCause.LIGHTNING;
            case MAGIC -> EntityDamageEvent.DamageCause.MAGIC;
            case MELTING -> EntityDamageEvent.DamageCause.MELTING;
            case POISON -> EntityDamageEvent.DamageCause.POISON;
            case PROJECTILE -> EntityDamageEvent.DamageCause.PROJECTILE;
            case SONIC_BOOM -> EntityDamageEvent.DamageCause.SONIC_BOOM;
            case STARVATION -> EntityDamageEvent.DamageCause.STARVATION;
            case SUFFOCATION -> EntityDamageEvent.DamageCause.SUFFOCATION;
            case SUICIDE -> EntityDamageEvent.DamageCause.SUICIDE;
            case THORNS -> EntityDamageEvent.DamageCause.THORNS;
            case VOID -> EntityDamageEvent.DamageCause.VOID;
            case WITHER -> EntityDamageEvent.DamageCause.WITHER;
            case WORLD_BORDER -> EntityDamageEvent.DamageCause.WORLD_BORDER;
            case DRAGON_BREATH -> EntityDamageEvent.DamageCause.DRAGON_BREATH;
            default -> throw new IllegalArgumentException("Unexpected value: " + cause);
        };
    }

    @SuppressWarnings("deprecation")
    public static DamageCause fromBukkit(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case BLOCK_EXPLOSION -> DamageCause.BLOCK_EXPLOSION;
            case CAMPFIRE -> DamageCause.CAMPFIRE;
            case CONTACT -> DamageCause.CONTACT;
            case CRAMMING -> DamageCause.CRAMMING;
            case CUSTOM -> DamageCause.CUSTOM;
            case DROWNING -> DamageCause.DROWNING;
            case DRYOUT -> DamageCause.DRYOUT;
            case ENTITY_ATTACK -> DamageCause.ENTITY_ATTACK;
            case ENTITY_EXPLOSION -> DamageCause.ENTITY_EXPLOSION;
            case ENTITY_SWEEP_ATTACK -> DamageCause.ENTITY_SWEEP_ATTACK;
            case FALL -> DamageCause.FALL;
            case FALLING_BLOCK -> DamageCause.FALLING_BLOCK;
            case FIRE -> DamageCause.FIRE;
            case FIRE_TICK -> DamageCause.FIRE_TICK;
            case FLY_INTO_WALL -> DamageCause.FLY_INTO_WALL;
            case FREEZE -> DamageCause.FREEZE;
            case HOT_FLOOR -> DamageCause.HOT_FLOOR;
            case KILL -> DamageCause.KILL;
            case LAVA -> DamageCause.LAVA;
            case LIGHTNING -> DamageCause.LIGHTNING;
            case MAGIC -> DamageCause.MAGIC;
            case MELTING -> DamageCause.MELTING;
            case POISON -> DamageCause.POISON;
            case PROJECTILE -> DamageCause.PROJECTILE;
            case SONIC_BOOM -> DamageCause.SONIC_BOOM;
            case STARVATION -> DamageCause.STARVATION;
            case SUFFOCATION -> DamageCause.SUFFOCATION;
            case SUICIDE -> DamageCause.SUICIDE;
            case THORNS -> DamageCause.THORNS;
            case VOID -> DamageCause.VOID;
            case WITHER -> DamageCause.WITHER;
            case WORLD_BORDER -> DamageCause.WORLD_BORDER;
            case DRAGON_BREATH -> DamageCause.DRAGON_BREATH;
            default -> throw new IllegalArgumentException("Unexpected value: " + cause);
        };
    }
}
