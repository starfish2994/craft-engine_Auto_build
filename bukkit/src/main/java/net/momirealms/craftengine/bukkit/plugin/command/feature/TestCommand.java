package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.CustomTridentUtils;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.*;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().itemManager().cachedSuggestions());
                    }
                }))
                .required("interpolationDuration", IntegerParser.integerParser())
                .required("displayType", ByteParser.byteParser((byte) 0, (byte) 8))
                .required("translation", StringParser.stringParser())
                .required("rotationLeft", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender();
                    NamespacedKey namespacedKey = context.get("id");
                    ItemStack item = new ItemStack(Material.TRIDENT);
                    item.editMeta((meta) -> {
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        container.set(CustomTridentUtils.customTridentKey, PersistentDataType.STRING, namespacedKey.asString());
                        container.set(CustomTridentUtils.interpolationDurationaKey, PersistentDataType.INTEGER, context.get("interpolationDuration"));
                        container.set(CustomTridentUtils.displayTypeKey, PersistentDataType.BYTE, context.get("displayType"));
                        container.set(CustomTridentUtils.translationKey, PersistentDataType.STRING, context.get("translation"));
                        container.set(CustomTridentUtils.rotationLeftKey, PersistentDataType.STRING, context.get("rotationLeft"));
                        Item<ItemStack> ceItem = BukkitItemManager.instance().createWrappedItem(Key.of(namespacedKey.asString()), null);
                        Optional<Integer> customModelData = ceItem.customModelData();
                        customModelData.ifPresent(meta::setCustomModelData);
                    });
                    player.getInventory().addItem(item);
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
