package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;

public class SetMaxVisibleFurnitureCommand extends BukkitCommandFeature<CommandSender> {

    public SetMaxVisibleFurnitureCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("max", IntegerParser.integerParser(1))
                .handler(context -> {
                    // 需要找一个更好的存储方案
                    BukkitServerPlayer cePlayer = plugin().adapt(context.sender());
                    Integer max = context.get("max");
                    cePlayer.setMaxVisibleFurniture(max, true);
                });
    }

    @Override
    public String getFeatureID() {
        return "set_max_visible_furniture";
    }
}
