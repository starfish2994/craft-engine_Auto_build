package net.momirealms.craftengine.bukkit.compatibility.model.freeminecraftmodels;

import com.magmaguy.freeminecraftmodels.customentity.StaticEntity;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.FoliaTask;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.entity.Entity;

public class FreeMinecraftModelsUtils {

    public static void bindModel(Entity base, String id) {
        StaticEntity staticEntity = StaticEntity.create(id, base.getLocation());
        if (staticEntity != null) {
            new FreeMinecraftModelsModelTask(staticEntity, base);
        }
    }

    public static class FreeMinecraftModelsModelTask implements Runnable {
        private final StaticEntity staticEntity;
        private final Entity base;
        private final SchedulerTask task;

        public FreeMinecraftModelsModelTask(StaticEntity staticEntity, Entity base) {
            this.staticEntity = staticEntity;
            this.base = base;
            if (VersionHelper.isFolia()) {
                this.task = new FoliaTask(base.getScheduler().runAtFixedRate(BukkitCraftEngine.instance().javaPlugin(), (t) -> this.run(), () -> {}, 1, 1));
            } else {
                this.task = BukkitCraftEngine.instance().scheduler().sync().runRepeating(this, 1, 1);
            }
        }

        @Override
        public void run() {
            if (!this.base.isValid()) {
                this.staticEntity.remove();
                this.task.cancel();
            }
        }
    }
}
