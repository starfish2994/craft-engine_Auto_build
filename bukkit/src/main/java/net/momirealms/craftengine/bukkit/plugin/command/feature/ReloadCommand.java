package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

public class ReloadCommand extends BukkitCommandFeature<CommandSender> {

    public ReloadCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s"))
                .handler(context -> {
                    long time1 = System.currentTimeMillis();
                    plugin().reload();
                    handleFeedback(context, MessageConstants.COMMAND_RELOAD_SUCCESS, Component.text(System.currentTimeMillis() - time1));
                });
    }

    @Override
    public String getFeatureID() {
        return "reload";
    }
}
