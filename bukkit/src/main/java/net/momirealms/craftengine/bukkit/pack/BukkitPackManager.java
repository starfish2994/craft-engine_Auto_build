package net.momirealms.craftengine.bukkit.pack;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.event.AsyncResourcePackGenerateEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.command.feature.ReloadCommand;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.pack.AbstractPackManager;
import net.momirealms.craftengine.core.pack.host.HostMode;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BukkitPackManager extends AbstractPackManager implements Listener {
	private final BukkitCraftEngine plugin;
	private HostMode previousHostMode = HostMode.NONE;
	private UUID previousHostUUID;

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
		// for 1.20.1 servers, not recommended to use
		if (Config.sendPackOnJoin() && !VersionHelper.isVersionNewerThan1_20_2()) {
			this.sendResourcePack(plugin.networkManager().getUser(event.getPlayer()), null);
		}
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

		// update server properties
		if (VersionHelper.isVersionNewerThan1_20_2()) {
			if (Config.hostMode() == HostMode.SELF_HOST) {
				if (Files.exists(resourcePackPath())) {
					updateResourcePackSettings(super.packUUID, ResourcePackHost.instance().url(), super.packHash, Config.kickOnDeclined(), Config.resourcePackPrompt());
				}
			} else if (Config.hostMode() == HostMode.EXTERNAL_HOST) {
				updateResourcePackSettings(Config.externalPackUUID(), Config.externalPackUrl(), Config.externalPackSha1(), Config.kickOnDeclined(), Config.resourcePackPrompt());
			}
		}

		if (Config.sendPackOnReload()) {
			if (this.previousHostMode == HostMode.SELF_HOST) {
				this.previousHostUUID = super.packUUID;
			}
			// unload packs if user changed to none host
			if (Config.hostMode() == HostMode.NONE && this.previousHostMode != HostMode.NONE) {
				unloadResourcePackForOnlinePlayers(this.previousHostUUID);
			}
			// load new external resource pack on reload
			if (Config.hostMode() == HostMode.EXTERNAL_HOST) {
				if (this.previousHostMode == HostMode.NONE) {
					updateResourcePackForOnlinePlayers(null);
				} else {
					updateResourcePackForOnlinePlayers(this.previousHostUUID);
				}
				// record previous host uuid here
				this.previousHostUUID = Config.externalPackUUID();
			}
			if (Config.hostMode() == HostMode.SELF_HOST && this.previousHostMode != HostMode.SELF_HOST) {
				if (ReloadCommand.RELOAD_PACK_FLAG) {
					ReloadCommand.RELOAD_PACK_FLAG = false;
					if (this.previousHostMode == HostMode.NONE) {
						updateResourcePackForOnlinePlayers(null);
					} else if (this.previousHostMode == HostMode.EXTERNAL_HOST) {
						updateResourcePackForOnlinePlayers(this.previousHostUUID);
					}
				}
			}
		}
		this.previousHostMode = Config.hostMode();
	}

	@Override
	public void unload() {
		super.unload();
		if (VersionHelper.isVersionNewerThan1_20_2() && this.previousHostMode != HostMode.NONE) {
			resetResourcePackSettings();
		}
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
		// update server properties
		if (VersionHelper.isVersionNewerThan1_20_2()) {
			if (Config.hostMode() == HostMode.SELF_HOST) {
				updateResourcePackSettings(super.packUUID, ResourcePackHost.instance().url(), super.packHash, Config.kickOnDeclined(), Config.resourcePackPrompt());
			}
		}
		// resend packs
		if (Config.hostMode() == HostMode.SELF_HOST && Config.sendPackOnReload()) {
			updateResourcePackForOnlinePlayers(this.previousHostUUID);
		}
	}

	protected void updateResourcePackForOnlinePlayers(UUID previousUUID) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BukkitServerPlayer serverPlayer = plugin.adapt(player);
			sendResourcePack(serverPlayer, previousUUID);
		}
	}

	private void resetResourcePackSettings() {
		try {
			Object settings = Reflections.field$DedicatedServer$settings.get(Reflections.method$MinecraftServer$getServer.invoke(null));
			Object properties = Reflections.field$DedicatedServerSettings$properties.get(settings);
			Reflections.field$DedicatedServerProperties$serverResourcePackInfo.set(properties, Optional.empty());
		} catch (Exception e) {
			this.plugin.logger().warn("Failed to update resource pack settings", e);
		}
	}

	private void updateResourcePackSettings(UUID uuid, String url, String sha1, boolean required, Component prompt) {
		if (!Config.sendPackOnJoin()) {
			resetResourcePackSettings();
			return;
		}
		try {
			Object settings = Reflections.field$DedicatedServer$settings.get(Reflections.method$MinecraftServer$getServer.invoke(null));
			Object properties = Reflections.field$DedicatedServerSettings$properties.get(settings);
            Object info;
            if (VersionHelper.isVersionNewerThan1_20_3()) {
                info = Reflections.constructor$ServerResourcePackInfo.newInstance(uuid, url, sha1, required, ComponentUtils.adventureToMinecraft(prompt));
            } else {
                info = Reflections.constructor$ServerResourcePackInfo.newInstance(url + uuid, sha1, required, ComponentUtils.adventureToMinecraft(prompt));
            }
            Reflections.field$DedicatedServerProperties$serverResourcePackInfo.set(properties, Optional.of(info));
        } catch (Exception e) {
			this.plugin.logger().warn("Failed to update resource pack settings", e);
		}
	}

	public void sendResourcePack(NetWorkUser user, @Nullable UUID previousPack) {
		if (Config.hostMode() == HostMode.NONE) return;
		String url;
		String sha1;
		UUID uuid;
		if (Config.hostMode() == HostMode.SELF_HOST) {
			url = ResourcePackHost.instance().url();
			sha1 = super.packHash;
			uuid = super.packUUID;
			if (!Files.exists(resourcePackPath())) return;
		} else {
			url = Config.externalPackUrl();
			sha1 = Config.externalPackSha1();
			uuid = Config.externalPackUUID();
			if (uuid.equals(previousPack)) return;
		}

		Object packPrompt = ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt());
		try {
			Object packPacket;
			if (VersionHelper.isVersionNewerThan1_20_5()) {
				packPacket = Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(
						uuid, url, sha1, Config.kickOnDeclined(), Optional.of(packPrompt)
				);
			} else if (VersionHelper.isVersionNewerThan1_20_3()) {
				packPacket = Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(
						uuid, url, sha1, Config.kickOnDeclined(), packPrompt
				);
			} else {
				packPacket = Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(
						url + uuid, sha1, Config.kickOnDeclined(), packPrompt
				);
			}
			if (user.decoderState() == ConnectionState.PLAY) {
				if (previousPack != null && VersionHelper.isVersionNewerThan1_20_3()) {
					plugin.networkManager().sendPackets(user, List.of(Reflections.constructor$ClientboundResourcePackPopPacket.newInstance(Optional.of(previousPack)), packPacket));
				} else {
					user.sendPacket(packPacket, false);
				}
			} else {
				user.nettyChannel().writeAndFlush(packPacket);
			}
		} catch (Exception e) {
			this.plugin.logger().warn("Failed to send resource pack", e);
		}
	}

	public void unloadResourcePackForOnlinePlayers(UUID uuid) {
		try {
			for (Player player : Bukkit.getOnlinePlayers()) {
				BukkitServerPlayer serverPlayer = plugin.adapt(player);
				if (serverPlayer.decoderState() == ConnectionState.PLAY) {
					serverPlayer.sendPacket(Reflections.constructor$ClientboundResourcePackPopPacket.newInstance(Optional.of(uuid)), true);
				}
			}
		} catch (Exception e) {
			this.plugin.logger().warn("Failed to unload online player resource pack", e);
		}
	}
}
