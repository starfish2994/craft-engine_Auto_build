package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.block.BukkitCustomBlock;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.sender.Sender;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.HashSet;
import java.util.Set;

public class DebugTargetBlockCommand extends BukkitCommandFeature<CommandSender> {

    public DebugTargetBlockCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .flag(manager.flagBuilder("this").build())
                .handler(context -> {
                    Player player = context.sender();
                    Block block;
                    if (context.flags().hasFlag("this")) {
                        Location location = player.getLocation();
                        block = location.getBlock();
                    } else {
                        block = player.getTargetBlockExact(10);
                        if (block == null) return;
                    }
                    String bData = block.getBlockData().getAsString();
                    Object blockState = BlockStateUtils.blockDataToBlockState(block.getBlockData());
                    Sender sender = plugin().senderFactory().wrap(context.sender());
                    sender.sendMessage(Component.text(bData));
                    Object holder = BukkitCraftEngine.instance().blockManager().getMinecraftBlockHolder(BlockStateUtils.blockStateToId(blockState));
                    if (holder != null) {
                        try {
                            @SuppressWarnings("unchecked")
                            Set<Object> tags = (Set<Object>) Reflections.field$Holder$Reference$tags.get(holder);
                            if (!tags.isEmpty()) {
                                sender.sendMessage(Component.text("tags: "));
                                for (Object tag : tags) {
                                    sender.sendMessage(Component.text(" - " + Reflections.field$TagKey$location.get(tag).toString()));
                                }
                            }
                        } catch (ReflectiveOperationException e) {
                            plugin().logger().warn("Could not get tags", e);
                        }
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_target_block";
    }
}
