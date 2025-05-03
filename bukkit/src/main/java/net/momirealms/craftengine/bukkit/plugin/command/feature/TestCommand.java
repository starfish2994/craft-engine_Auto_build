package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().itemManager().cachedSuggestions());
                    }
                }))
                .required("interpolationDelay", IntegerParser.integerParser())
                .required("transformationInterpolationDuration", IntegerParser.integerParser())
                .required("positionRotationInterpolationDuration", IntegerParser.integerParser())
                .handler(context -> {
                    Player player = context.sender();
                    NamespacedKey namespacedKey = context.get("id");
                    var item = ItemStack.of(Material.TRIDENT);
                    item.editPersistentDataContainer(container -> {
                        container.set(Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident")), PersistentDataType.STRING, namespacedKey.asString());
                        container.set(Objects.requireNonNull(NamespacedKey.fromString("craftengine:interpolation_delay")), PersistentDataType.INTEGER, context.get("interpolationDelay"));
                        container.set(Objects.requireNonNull(NamespacedKey.fromString("craftengine:transformation_interpolation_duration")), PersistentDataType.INTEGER, context.get("transformationInterpolationDuration"));
                        container.set(Objects.requireNonNull(NamespacedKey.fromString("craftengine:position_rotation_interpolation_duration")), PersistentDataType.INTEGER, context.get("positionRotationInterpolationDuration"));
                    });
                    player.getInventory().addItem(item);
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
