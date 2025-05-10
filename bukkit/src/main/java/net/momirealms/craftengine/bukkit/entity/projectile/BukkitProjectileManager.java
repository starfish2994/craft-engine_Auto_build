package net.momirealms.craftengine.bukkit.entity.projectile;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.entity.projectile.CustomProjectile;
import net.momirealms.craftengine.core.entity.projectile.ProjectileManager;
import net.momirealms.craftengine.core.entity.projectile.ProjectileMeta;
import net.momirealms.craftengine.core.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitProjectileManager implements Listener, ProjectileManager {
    private static BukkitProjectileManager instance;
    private final BukkitCraftEngine plugin;
    private final Map<Integer, BukkitCustomProjectile> projectiles;

    public BukkitProjectileManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        this.projectiles = new ConcurrentHashMap<>();
        instance = this;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin.bootstrap());
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public Optional<CustomProjectile> projectileByEntityId(int entityId) {
        return Optional.ofNullable(this.projectiles.get(entityId));
    }

    @EventHandler(ignoreCancelled = true,  priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ItemStack projectileItem;
        if (projectile instanceof ThrowableProjectile throwableProjectile) {
            projectileItem = throwableProjectile.getItem();
        } else if (projectile instanceof Arrow arrow) {
            projectileItem = arrow.getItemStack();
        } else {
            return;
        }
        System.out.println("发射");
        Item<ItemStack> wrapped = this.plugin.itemManager().wrap(projectileItem);
        if (wrapped == null) return;
        wrapped.getCustomItem().ifPresent(it -> {
            ProjectileMeta meta = it.settings().projectileMeta();
            if (meta != null) {
                System.out.println("来啦");
                this.projectiles.put(projectile.getEntityId(), new BukkitCustomProjectile(meta, projectile, wrapped));
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityRemove(EntityRemoveEvent event) {
        this.projectiles.remove(event.getEntity().getEntityId());
    }

    public static BukkitProjectileManager instance() {
        return instance;
    }
}
