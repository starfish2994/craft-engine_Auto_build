package net.momirealms.craftengine.bukkit.compatibility.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class LuckPermsEventListeners {
    private final JavaPlugin plugin;
    private final LuckPerms luckPerms;
    private final BiConsumer<UUID, Boolean> consumer;
    private final SchedulerAdapter<World> scheduler;
    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();

    public LuckPermsEventListeners(JavaPlugin plugin, BiConsumer<UUID, Boolean> consumer, SchedulerAdapter<World> scheduler) {
        this.plugin = plugin;
        this.consumer = consumer;
        this.scheduler = scheduler;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            EventBus eventBus = this.luckPerms.getEventBus();
            EventSubscription<UserDataRecalculateEvent> onUserPermissionChangeSubscription =
                    eventBus.subscribe(
                            plugin,
                            UserDataRecalculateEvent.class,
                            this::onUserPermissionChange
                    );
            EventSubscription<GroupDataRecalculateEvent> onGroupPermissionChangeSubscription =
                    eventBus.subscribe(
                            plugin,
                            GroupDataRecalculateEvent.class,
                            this::onGroupPermissionChange
                    );
            this.subscriptions.add(onUserPermissionChangeSubscription);
            this.subscriptions.add(onGroupPermissionChangeSubscription);
        } else luckPerms = null;
    }

    public void unregisterListeners() {
        for (EventSubscription<?> subscription : this.subscriptions) {
            try {
                subscription.close();
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to close event subscription", e);
            }
        }
        this.subscriptions.clear();
    }

    private void onUserPermissionChange(UserDataRecalculateEvent event) {
        this.consumer.accept(event.getUser().getUniqueId(), true);
    }

    private void onGroupPermissionChange(GroupDataRecalculateEvent event) {
        this.scheduler.asyncLater(() -> {
            String groupName = event.getGroup().getName();
            Bukkit.getOnlinePlayers().forEach(player -> {
                UUID playerUUID = player.getUniqueId();
                User onlineUser = this.luckPerms.getUserManager().getUser(playerUUID);
                if (onlineUser == null) return;
                boolean isInGroup = onlineUser.getInheritedGroups(onlineUser.getQueryOptions())
                        .parallelStream()
                        .anyMatch(g -> g.getName().equals(groupName));
                if (isInGroup) {
                    this.consumer.accept(playerUUID, false);
                }
            });
        }, 1L, TimeUnit.SECONDS);
    }
}
