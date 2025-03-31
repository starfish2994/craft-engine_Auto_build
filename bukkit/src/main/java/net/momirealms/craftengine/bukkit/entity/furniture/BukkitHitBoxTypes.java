package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.HitBoxTypes;

public class BukkitHitBoxTypes extends HitBoxTypes {

    public static void init() {}

    static {
        register(INTERACTION, InteractionHitBox.FACTORY);
        register(SHULKER, ShulkerHitBox.FACTORY);
        register(BOAT, HappyGhastHitBox.FACTORY);
    }
}
