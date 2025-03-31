package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .handler(context -> {
                    Player player = context.sender();
                    Location location = player.getLocation();
                    try {
                        Object level = Reflections.field$CraftWorld$ServerLevel.get(player.getWorld());
                        Object aabb = FastNMS.INSTANCE.constructor$AABB(location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                                location.getBlockX() + 1, location.getBlockY() + 1, location.getBlockZ() + 1);
                        CollisionEntity nmsEntity = FastNMS.INSTANCE.createCollisionEntity(level, aabb, location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5, false);
                        FastNMS.INSTANCE.method$LevelWriter$addFreshEntity(level, nmsEntity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
