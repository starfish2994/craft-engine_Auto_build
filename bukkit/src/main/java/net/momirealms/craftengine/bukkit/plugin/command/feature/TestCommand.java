package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.parser.standard.StringArrayParser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("location", LocationParser.locationParser())
                // .required("remove", StringArrayParser.stringArrayParser())
                .handler(context -> {
                    Player player = context.sender();
                    // String[] removeEntityIds = context.get("remove");
                    // int removeHitboxId = Integer.parseInt(removeEntityIds[0]);
                    // int removePlayerId = Integer.parseInt(removeEntityIds[1]);
                    // if (removeHitboxId >= 0 && removePlayerId >= 0) {
                    //     try {
                    //         Object packet = NetworkReflections.constructor$ClientboundRemoveEntitiesPacket.newInstance((Object) new int[]{removeHitboxId, removePlayerId});
                    //         plugin().adapt(player).sendPacket(packet, true);
                    //         player.sendMessage("发送成功");
                    //     } catch (ReflectiveOperationException e) {
                    //         player.sendMessage("发送失败");
                    //     }
                    //     return;
                    // }
                    Location location = context.get("location");
                    // int hitboxId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
                    int playerId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
                    List<Object> packets = new ArrayList<>();
                    // packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                    //         hitboxId, UUID.randomUUID(), location.x(), location.y(), location.z(), 0, location.getYaw(),
                    //         MEntityTypes.HAPPY_GHAST, 0, CoreReflections.instance$Vec3$Zero, 0
                    // ));
                    packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                            playerId, UUID.randomUUID(), location.x(), location.y()/* + 4*/, location.z(), 0, location.getYaw(),
                            MEntityTypes.PLAYER, 0, CoreReflections.instance$Vec3$Zero, 0
                    ));
                    player.sendMessage("player: " + MEntityTypes.PLAYER);
                    plugin().adapt(player).sendPackets(packets, true);
                    player.sendMessage("发送成功 id: [" + /*hitboxId + ", " +*/ playerId + "]");
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
