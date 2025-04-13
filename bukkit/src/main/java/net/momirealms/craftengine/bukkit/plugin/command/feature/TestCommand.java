package net.momirealms.craftengine.bukkit.plugin.command.feature;

import com.saicone.rtag.RtagItem;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;

import java.util.Map;

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
                    ItemStack itemStack = new ItemStack(Material.STONE);
                    RtagItem rtagItem = new RtagItem(itemStack);
                    rtagItem.setComponent(ComponentKeys.CUSTOM_DATA, Map.of("test1", "1"));
                    rtagItem.removeComponent(ComponentKeys.CUSTOM_DATA);
                    rtagItem.removeComponent(ComponentKeys.LORE);
                    player.getInventory().addItem(rtagItem.load());
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
