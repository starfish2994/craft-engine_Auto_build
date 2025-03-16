package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SelectorItemRecipeBrowserCommand extends BukkitCommandFeature<CommandSender> {

    public SelectorItemRecipeBrowserCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(FlagKeys.BROWSE_FLAG)
                .required("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser(true))
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().itemManager().cachedSuggestions());
                    }
                }))
                .handler(context -> {
                    MultiplePlayerSelector selector = context.get("player");
                    Collection<Player> players = selector.values();
                    for (Player player : players) {
                        BukkitServerPlayer serverPlayer = plugin().adapt(player);
                        NamespacedKey namespacedKey = context.get("id");
                        Key itemId = Key.of(namespacedKey.namespace(), namespacedKey.value());
                        List<Recipe<Object>> inRecipes = plugin().recipeManager().getRecipeByResult(itemId);
                        if (!inRecipes.isEmpty()) {
                            plugin().itemBrowserManager().openRecipePage(serverPlayer, null, inRecipes, 0, 0);
                        } else if (context.flags().hasFlag(FlagKeys.BROWSE)) {
                            plugin().itemBrowserManager().openNoRecipePage(serverPlayer, itemId, null, 0);
                        } else {
                            handleFeedback(context, MessageConstants.COMMAND_ITEM_RECIPE_BROWSER_RECIPE_NO_FOUND);
                        }
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "selector_item_recipe_browser";
    }
}
