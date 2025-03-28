package net.momirealms.craftengine.bukkit.compatibility.slimeworld;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.events.LoadSlimeWorldEvent;
import com.infernalsuite.asp.api.world.SlimeWorld;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldManager;
import net.momirealms.craftengine.core.world.chunk.storage.DefaultStorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class SlimeFormatStorageAdaptor extends DefaultStorageAdaptor implements Listener {
    private final WorldManager worldManager;

    @EventHandler
    public void onWorldLoad(LoadSlimeWorldEvent event) {
        org.bukkit.World world = Bukkit.getWorld(event.getSlimeWorld().getName());
        this.worldManager.loadWorld(this.worldManager.wrap(world));
    }

    public SlimeFormatStorageAdaptor(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public SlimeWorld getWorld(String name) {
        return AdvancedSlimePaperAPI.instance().getLoadedWorld(name);
    }

    @Override
    public @NotNull WorldDataStorage adapt(@NotNull World world) {
        SlimeWorld slimeWorld = getWorld(world.name());
        if (slimeWorld == null) {
            return super.adapt(world);
        }
        return new SlimeWorldDataStorage(slimeWorld);
    }
}
