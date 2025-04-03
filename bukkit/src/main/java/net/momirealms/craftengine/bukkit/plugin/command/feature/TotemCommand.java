package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.MaterialUtils;
import net.momirealms.craftengine.bukkit.util.PlayerUtils;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class TotemCommand extends BukkitCommandFeature<CommandSender> {

    public TotemCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(FlagKeys.SILENT_FLAG)
                .required("players", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().itemManager().cachedTotemSuggestions());
                    }
                }))
                .handler(context -> {
                    NamespacedKey namespacedKey = context.get("id");
                    Key key = Key.of(namespacedKey.namespace(), namespacedKey.value());
                    CustomItem<ItemStack> item = plugin().itemManager().getCustomItem(key).orElse(null);
                    if (item == null) {
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_GET_FAILURE_NOT_EXIST, Component.text(key.toString()));
                        return;
                    }
                    if (MaterialUtils.getMaterial(item.material()) != Material.TOTEM_OF_UNDYING) {
                        handleFeedback(context, MessageConstants.COMMAND_TOTEM_NOT_TOTEM, Component.text(key.toString()));
                        return;
                    }

                    ItemStack totem = item.buildItemStack();
                    MultiplePlayerSelector selector = context.get("players");
                    for (Player player : selector.values()) {
                        PlayerUtils.sendTotemAnimation(player, totem);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "totem";
    }
}
