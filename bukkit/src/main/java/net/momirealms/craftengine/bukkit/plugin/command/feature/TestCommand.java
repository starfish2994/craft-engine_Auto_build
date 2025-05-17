package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurnitureManager;
import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class TestCommand extends BukkitCommandFeature<CommandSender> {
    public static final Collection<Suggestion> TARGET_BLOCK_SUGGESTIONS = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            TARGET_BLOCK_SUGGESTIONS.add(Suggestion.suggestion(material.getKey().toString()));
        }
    }

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                // .required("reset", BooleanParser.booleanParser())
                // .required("setTag", NamespacedKeyParser.namespacedKeyParser())
                // .required("targetBlock", StringParser.stringComponent(StringParser.StringMode.GREEDY_FLAG_YIELDING).suggestionProvider(new SuggestionProvider<>() {
                //     @Override
                //     public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                //         return CompletableFuture.completedFuture(TARGET_BLOCK_SUGGESTIONS);
                //     }
                // }))
                .handler(context -> {
                    Player player = context.sender();
                    BukkitServerPlayer cePlayer = plugin().adapt(player);
                    player.sendMessage("visualFurnitureView1: " + cePlayer.visualFurnitureView().getTotalMembers());
                    cePlayer.visualFurnitureView().getAllElements().forEach(element -> {
                        LoadedFurniture furniture = BukkitFurnitureManager.instance().loadedFurnitureByRealEntityId(element.entityId());
                        if (furniture == null || !player.canSee(furniture.baseEntity())) {
                            cePlayer.visualFurnitureView().removeByEntityId(element.entityId());
                            player.sendMessage("remove: " + element.entityId());
                        }
                    });
                    player.sendMessage("visualFurnitureView2: " + cePlayer.visualFurnitureView().getTotalMembers());
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
