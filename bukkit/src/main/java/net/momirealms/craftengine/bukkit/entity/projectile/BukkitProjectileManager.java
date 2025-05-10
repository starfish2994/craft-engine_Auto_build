package net.momirealms.craftengine.bukkit.entity.projectile;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.FoliaTask;
import net.momirealms.craftengine.bukkit.util.ParticleUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.projectile.CustomProjectile;
import net.momirealms.craftengine.core.entity.projectile.ProjectileManager;
import net.momirealms.craftengine.core.entity.projectile.ProjectileMeta;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
        handleProjectileLoad(projectile, true);
    }

    @EventHandler(ignoreCancelled = true,  priority = EventPriority.HIGHEST)
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Projectile projectile) {
                handleProjectileLoad(projectile, false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        this.projectiles.remove(event.getEntity().getEntityId());
    }

    private void handleProjectileLoad(Projectile projectile, boolean delay) {
        ItemStack projectileItem;
        if (projectile instanceof ThrowableProjectile throwableProjectile) {
            projectileItem = throwableProjectile.getItem();
        } else if (projectile instanceof Arrow arrow) {
            projectileItem = arrow.getItemStack();
        } else {
            return;
        }
        Item<ItemStack> wrapped = this.plugin.itemManager().wrap(projectileItem);
        if (wrapped == null) return;
        wrapped.getCustomItem().ifPresent(it -> {
            ProjectileMeta meta = it.settings().projectileMeta();
            if (meta != null) {
                this.projectiles.put(projectile.getEntityId(), new BukkitCustomProjectile(meta, projectile, wrapped));
                ProjectileInjectTask task = new ProjectileInjectTask(projectile);
                if (!delay) {
                    task.run();
                } else if (VersionHelper.isFolia()) {

                } else {
                    plugin.scheduler().sync().runDelayed(task);
                }
            }
        });
    }

    public class ProjectileInjectTask implements Runnable {
        private final Projectile projectile;
        private final SchedulerTask task;
        private boolean injected;

        public ProjectileInjectTask(Projectile projectile) {
            this.projectile = projectile;
            if (VersionHelper.isFolia()) {
                this.task = new FoliaTask(projectile.getScheduler().runAtFixedRate(plugin.bootstrap(), (t) -> this.run(), () -> {}, 1, 1));
            } else {
                this.task = plugin.scheduler().sync().runRepeating(this, 1, 1);
            }
        }

        @Override
        public void run() {
            if (!this.projectile.isValid()) {
                this.task.cancel();
                return;
            }
            Object nmsEntity = FastNMS.INSTANCE.method$CraftEntity$getHandle(this.projectile);
            if (!this.injected) {
                Object trackedEntity = FastNMS.INSTANCE.field$Entity$trackedEntity(nmsEntity);
                if (trackedEntity == null) {
                    return;
                }
                Object serverEntity = FastNMS.INSTANCE.filed$ChunkMap$TrackedEntity$serverEntity(trackedEntity);
                if (serverEntity == null) {
                    return;
                }
                try {
                    Reflections.field$ServerEntity$updateInterval.set(serverEntity, 1);
                    this.injected = true;
                } catch (ReflectiveOperationException e) {
                    plugin.logger().warn("Failed to update server entity tracking interval", e);
                }
            }
            if (canSpawnParticle(nmsEntity)) {
                this.projectile.getWorld().spawnParticle(ParticleUtils.BUBBLE, this.projectile.getLocation(), 3, 0.1, 0.1, 0.1, 0);
            }
        }

        private static boolean canSpawnParticle(Object nmsEntity) {
            if (!FastNMS.INSTANCE.field$Entity$wasTouchingWater(nmsEntity)) return false;
            if (Reflections.clazz$AbstractArrow.isInstance(nmsEntity)) {
                return !FastNMS.INSTANCE.method$AbstractArrow$isInGround(nmsEntity);
            }
            return true;
        }
    }

    public static BukkitProjectileManager instance() {
        return instance;
    }
}
