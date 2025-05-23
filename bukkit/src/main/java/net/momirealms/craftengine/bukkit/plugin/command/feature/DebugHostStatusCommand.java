package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.injector.BukkitInjector;
import net.momirealms.craftengine.core.pack.host.impl.SelfHostHttpServer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.sender.Sender;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;

public class DebugHostStatusCommand extends BukkitCommandFeature<CommandSender> {

    public DebugHostStatusCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .handler(context -> {
                    Sender sender = plugin().senderFactory().wrap(context.sender());
                    sender.sendMessage(Component.text("Self Host status: " + (SelfHostHttpServer.instance().isAlive() ? "on" : "off")));
                    byte[] pack = SelfHostHttpServer.instance().resourcePackBytes();
                    sender.sendMessage(Component.text("Resource Pack Bytes: " + (pack == null ? "null" : pack.length)));
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_host_status";
    }
}
