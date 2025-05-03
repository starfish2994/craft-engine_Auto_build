package net.momirealms.craftengine.bukkit.plugin.command.feature;

import com.saicone.rtag.util.ChatComponent;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.StringParser;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("text", StringParser.greedyStringParser())
                .handler(context -> {
                    plugin().senderFactory().wrap(context.sender()).sendMessage(Component.text(
                            ChatComponent.toTag(ComponentUtils.adventureToMinecraft(AdventureHelper.miniMessage().deserialize(context.get("text")))).toString()
                    ));
                    plugin().senderFactory().wrap(context.sender()).sendMessage(AdventureHelper.miniMessage().deserialize(context.get("text")));
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
