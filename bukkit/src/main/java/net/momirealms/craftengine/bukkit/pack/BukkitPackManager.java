package net.momirealms.craftengine.bukkit.pack;

import net.momirealms.craftengine.bukkit.api.event.AsyncResourcePackGenerateEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.core.pack.AbstractPackManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class BukkitPackManager extends AbstractPackManager implements Listener {
	private final BukkitCraftEngine plugin;

	public BukkitPackManager(BukkitCraftEngine plugin) {
        super(plugin, (rf, zp) -> {
			AsyncResourcePackGenerateEvent endEvent = new AsyncResourcePackGenerateEvent(rf, zp);
			EventUtils.fireAndForget(endEvent);
		});
        this.plugin = plugin;
	}

	@Override
	public void delayedInit() {
		super.delayedInit();
		Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		// todo 1.20.1 资源包发送
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
		// for 1.20.1 servers, not recommended to use
		if (Config.sendPackOnJoin() && Config.kickOnDeclined() && !VersionHelper.isVersionNewerThan1_20_2()) {
			if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED || event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
				event.getPlayer().kick();
			}
		}
	}

	@Override
	public void load() {
		super.load();
	}

	@Override
	public void unload() {
		super.unload();
	}

	@Override
	public void disable() {
		super.disable();
		HandlerList.unregisterAll(this);
	}

	@Override
	public void generateResourcePack() {
		// generate pack
		super.generateResourcePack();
	}
}
