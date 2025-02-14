package net.momirealms.craftengine.bukkit.item.recipe;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ItemUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.item.recipe.CookingInput;
import net.momirealms.craftengine.core.item.recipe.OptimizedIDItem;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.item.recipe.input.CraftingInput;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeEventListener implements Listener {
    private static final OptimizedIDItem<ItemStack> EMPTY = new OptimizedIDItem<>(null, null);
    private final ItemManager<ItemStack> itemManager;
    private final BukkitRecipeManager recipeManager;
    private final BukkitCraftEngine plugin;

    public RecipeEventListener(BukkitCraftEngine plugin, BukkitRecipeManager recipeManager, ItemManager<ItemStack> itemManager) {
        this.itemManager = itemManager;
        this.recipeManager = recipeManager;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickCartographyTable(InventoryClickEvent event) {
        if (VersionHelper.isPaper()) return;
        if (!(event.getClickedInventory() instanceof CartographyInventory cartographyInventory)) {
            return;
        }
        plugin.scheduler().sync().runDelayed(() -> {
            if (ItemUtils.hasCustomItem(cartographyInventory.getContents())) {
                cartographyInventory.setResult(new ItemStack(Material.AIR));
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (BlockState state : event.getChunk().getTileEntities()) {

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpecialRecipe(PrepareItemCraftEvent event) {
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe == null)
            return;
        if (!(recipe instanceof ComplexRecipe))
            return;
        CraftingInventory inventory = event.getInventory();
        if (ItemUtils.hasCustomItem(inventory.getMatrix())) {
            inventory.setResult(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftingRecipe(PrepareItemCraftEvent event) {
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe == null)
            return;

        // we only handle shaped and shapeless recipes
        boolean shapeless = event.getRecipe() instanceof ShapelessRecipe;
        boolean shaped = event.getRecipe() instanceof ShapedRecipe;
        if (!shaped && !shapeless) return;

        CraftingRecipe craftingRecipe = (CraftingRecipe) recipe;
        Key recipeId = Key.of(craftingRecipe.getKey().namespace(), craftingRecipe.getKey().value());

        CraftingInventory inventory = event.getInventory();
        ItemStack[] ingredients = inventory.getMatrix();

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);

        // if the recipe is a vanilla one but not injected, custom items should never be ingredients
        if (this.recipeManager.isDataPackRecipe(recipeId) && !isCustom) {
            if (ItemUtils.hasCustomItem(ingredients)) {
                inventory.setResult(null);
            }
            return;
        }

        // Maybe it's recipe from other plugins, then we ignore it
        if (!isCustom) {
            return;
        }

        List<OptimizedIDItem<ItemStack>> optimizedIDItems = new ArrayList<>();
        for (ItemStack itemStack : ingredients) {
            if (ItemUtils.isEmpty(itemStack)) {
                optimizedIDItems.add(EMPTY);
            } else {
                Item<ItemStack> wrappedItem = this.itemManager.wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    // an invalid item is used in recipe, we disallow it
                    inventory.setResult(null);
                    return;
                } else {
                    optimizedIDItems.add(new OptimizedIDItem<>(idHolder.get(), itemStack));
                }
            }
        }

        CraftingInput<ItemStack> input;
        if (ingredients.length == 9) {
            input = CraftingInput.of(3, 3, optimizedIDItems);
        } else if (ingredients.length == 4) {
            input = CraftingInput.of(2, 2, optimizedIDItems);
        } else {
            return;
        }

        Player player;
        try {
            player = (Player) Reflections.method$InventoryView$getPlayer.invoke(event.getView());
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to get inventory viewer", e);
            return;
        }

        BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
        Recipe<ItemStack> lastRecipe = serverPlayer.lastUsedRecipe();
        if (lastRecipe != null && (lastRecipe.type() == RecipeTypes.SHAPELESS || lastRecipe.type() == RecipeTypes.SHAPED )) {
            if (lastRecipe.matches(input)) {
                inventory.setResult(lastRecipe.getResult(serverPlayer));
                return;
            }
        }

        Recipe<ItemStack> ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SHAPELESS, input);
        if (ceRecipe != null) {
            inventory.setResult(ceRecipe.getResult(serverPlayer));
            serverPlayer.setLastUsedRecipe(ceRecipe);
            correctCraftingRecipeUsed(inventory, ceRecipe);
            return;
        }
        ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SHAPED, input);
        if (ceRecipe != null) {
            inventory.setResult(ceRecipe.getResult(serverPlayer));
            serverPlayer.setLastUsedRecipe(ceRecipe);
            correctCraftingRecipeUsed(inventory, ceRecipe);
            return;
        }
        // clear result if not met
        inventory.setResult(null);
    }

    private void correctCraftingRecipeUsed(CraftingInventory inventory, Recipe<ItemStack> recipe) {
        Object holderOrRecipe = recipeManager.getRecipeHolderByRecipe(recipe);
        if (holderOrRecipe == null) return;
        try {
            Object resultInventory = Reflections.field$CraftInventoryCrafting$resultInventory.get(inventory);
            Reflections.field$ResultContainer$recipeUsed.set(resultInventory, holderOrRecipe);
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to correct used recipe", e);
        }
    }

    // TODO find a lighter way
    @EventHandler(ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        if (!(event.getBlock().getState() instanceof Furnace furnace)) {
            return;
        }
        FurnaceInventory inventory = furnace.getInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        CookingRecipe<?> recipe = event.getRecipe();

        Key recipeId = Key.of(recipe.getKey().namespace(), recipe.getKey().value());
        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        ItemStack sourceItem = event.getSource();

        if (this.recipeManager.isDataPackRecipe(recipeId) && !isCustom) {
            if (ItemUtils.isCustomItem(sourceItem)) {
                event.setTotalCookTime(Integer.MAX_VALUE);
            }
            return;
        }

        if (!isCustom) {
            return;
        }

        Item<ItemStack> wrappedItem = this.itemManager.wrap(sourceItem);
        Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
        if (idHolder.isEmpty()) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        CookingInput<ItemStack> input = new CookingInput<>(new OptimizedIDItem<>(idHolder.get(), event.getSource()));
        net.momirealms.craftengine.core.item.recipe.CookingRecipe<ItemStack> ceRecipe;
        if (recipe instanceof FurnaceRecipe) {
            ceRecipe = (net.momirealms.craftengine.core.item.recipe.CookingRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.SMELTING, input);
        } else if (recipe instanceof SmokingRecipe) {
            ceRecipe = (net.momirealms.craftengine.core.item.recipe.CookingRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.SMOKING, input);
        } else if (recipe instanceof BlastingRecipe) {
            ceRecipe = (net.momirealms.craftengine.core.item.recipe.CookingRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.BLASTING, input);
        } else {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        if (ceRecipe == null) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        event.setTotalCookTime(ceRecipe.cookingTime());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        CookingRecipe<?> recipe = event.getRecipe();
        if (recipe == null) return;

        Key recipeId = Key.of(recipe.getKey().namespace(), recipe.getKey().value());
        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);

        if (this.recipeManager.isDataPackRecipe(recipeId) && !isCustom) {
            if (ItemUtils.isCustomItem(event.getSource())) {
                event.setCancelled(true);
            }
            return;
        }

        if (!isCustom) {
            return;
        }

        Item<ItemStack> wrappedItem = this.itemManager.wrap(event.getSource());
        Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
        if (idHolder.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        CookingInput<ItemStack> input = new CookingInput<>(new OptimizedIDItem<>(idHolder.get(), event.getSource()));
        if (recipe instanceof FurnaceRecipe furnaceRecipe) {
            Recipe<ItemStack> ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SMELTING, input);
            if (ceRecipe != null) {
                event.setResult(ceRecipe.getResult(null));
                return;
            }
        } else if (recipe instanceof SmokingRecipe smokingRecipe) {
            Recipe<ItemStack> ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SMOKING, input);
            if (ceRecipe != null) {
                event.setResult(ceRecipe.getResult(null));
                return;
            }
        } else if (recipe instanceof BlastingRecipe blastingRecipe) {
            Recipe<ItemStack> ceRecipe = this.recipeManager.getRecipe(RecipeTypes.BLASTING, input);
            if (ceRecipe != null) {
                event.setResult(ceRecipe.getResult(null));
                return;
            }
        }

        event.setCancelled(true);
    }
}
