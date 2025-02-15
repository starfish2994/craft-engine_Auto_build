package net.momirealms.craftengine.bukkit.item.recipe;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.injector.BukkitInjector;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ItemUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.item.recipe.CustomCampfireRecipe;
import net.momirealms.craftengine.core.item.recipe.OptimizedIDItem;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.item.recipe.input.CookingInput;
import net.momirealms.craftengine.core.item.recipe.input.CraftingInput;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFurnaceInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof FurnaceInventory furnaceInventory)) {
            return;
        }
        Furnace furnace = furnaceInventory.getHolder();
        try {
            Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(furnace);
            BukkitInjector.injectCookingBlockEntity(blockEntity);
        } catch (Exception e) {
            plugin.logger().warn("Failed to inject cooking block entity", e);
        }
    }

    // for 1.20.1-1.21.1
    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (VersionHelper.isVersionNewerThan1_21_2()) return;
        Block block = event.getBlock();
        Material material = block.getType();
        if (material == Material.CAMPFIRE) {
            if (block.getState() instanceof Campfire campfire) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(campfire);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    this.plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceBlock(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        if (material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER) {
            if (block.getState() instanceof Furnace furnace) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(furnace);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        } else if (!VersionHelper.isVersionNewerThan1_21_2() && material == Material.CAMPFIRE) {
            if (block.getState() instanceof Campfire campfire) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(campfire);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    this.plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        }
    }

    // for 1.21.2+
    @EventHandler(ignoreCancelled = true)
    public void onPutItemOnCampfire(PlayerInteractEvent event) {
        if (!VersionHelper.isVersionNewerThan1_21_2()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        Material type = clicked.getType();
        if (type != Material.CAMPFIRE && type != Material.SOUL_CAMPFIRE) return;
        if (!VersionHelper.isVersionNewerThan1_21_2()) {
            if (clicked.getState() instanceof Campfire campfire) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(campfire);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    this.plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        }

        ItemStack itemStack = event.getItem();
        if (ItemUtils.isEmpty(itemStack)) return;
        try {
            @SuppressWarnings("unchecked")
            Optional<Object> optionalMCRecipe = (Optional<Object>) Reflections.method$RecipeManager$getRecipeFor1.invoke(
                    BukkitRecipeManager.minecraftRecipeManager(),
                    Reflections.instance$RecipeType$CAMPFIRE_COOKING,
                    Reflections.constructor$SingleRecipeInput.newInstance(Reflections.method$CraftItemStack$asNMSMirror.invoke(null, itemStack)),
                    Reflections.field$CraftWorld$ServerLevel.get(event.getPlayer().getWorld()),
                    null
            );
            if (optionalMCRecipe.isEmpty()) {
                return;
            }
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
            if (idHolder.isEmpty()) {
                return;
            }
            CookingInput<ItemStack> input = new CookingInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
            CustomCampfireRecipe<ItemStack> ceRecipe = (CustomCampfireRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.CAMPFIRE_COOKING, input);
            if (ceRecipe == null) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to handle interact campfire", e);
        }
    }

    // for 1.21.2+
    @EventHandler(ignoreCancelled = true)
    public void onCampfireCook(CampfireStartEvent event) {
        if (!VersionHelper.isVersionNewerThan1_21_2()) return;
        CampfireRecipe recipe = event.getRecipe();
        Key recipeId = new Key(recipe.getKey().namespace(), recipe.getKey().value());

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        if (!isCustom) {
            return;
        }

        ItemStack itemStack = event.getSource();
        Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
        Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
        if (idHolder.isEmpty()) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        CookingInput<ItemStack> input = new CookingInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
        CustomCampfireRecipe<ItemStack> ceRecipe = (CustomCampfireRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.CAMPFIRE_COOKING, input);
        if (ceRecipe == null) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        event.setTotalCookTime(ceRecipe.cookingTime());
    }

    // for 1.21.2+
    @EventHandler(ignoreCancelled = true)
    public void onCampfireCook(BlockCookEvent event) {
        if (!VersionHelper.isVersionNewerThan1_21_2()) return;
        Material type = event.getBlock().getType();
        if (type != Material.CAMPFIRE && type != Material.SOUL_CAMPFIRE) return;
        CampfireRecipe recipe = (CampfireRecipe) event.getRecipe();
        Key recipeId = new Key(recipe.getKey().namespace(), recipe.getKey().value());

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        if (!isCustom) {
            return;
        }

        ItemStack itemStack = event.getSource();
        Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
        Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
        if (idHolder.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        CookingInput<ItemStack> input = new CookingInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
        CustomCampfireRecipe<ItemStack> ceRecipe = (CustomCampfireRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.CAMPFIRE_COOKING, input);
        if (ceRecipe == null) {
            event.setCancelled(true);
            return;
        }

        event.setResult(ceRecipe.getResult(null));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickCartographyTable(InventoryClickEvent event) {
        if (VersionHelper.isPaper()) return;
        if (!(event.getClickedInventory() instanceof CartographyInventory cartographyInventory)) {
            return;
        }
        this.plugin.scheduler().sync().runDelayed(() -> {
            if (ItemUtils.hasCustomItem(cartographyInventory.getContents())) {
                cartographyInventory.setResult(new ItemStack(Material.AIR));
            }
        });
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

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        // Maybe it's recipe from other plugins, then we ignore it
        if (!isCustom) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] ingredients = inventory.getMatrix();

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
        Key lastRecipe = serverPlayer.lastUsedRecipe();

        Recipe<ItemStack> ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SHAPELESS, input, lastRecipe);
        if (ceRecipe != null) {
            inventory.setResult(ceRecipe.getResult(serverPlayer));
            serverPlayer.setLastUsedRecipe(ceRecipe.id());
            correctCraftingRecipeUsed(inventory, ceRecipe);
            return;
        }
        ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SHAPED, input, lastRecipe);
        if (ceRecipe != null) {
            inventory.setResult(ceRecipe.getResult(serverPlayer));
            serverPlayer.setLastUsedRecipe(ceRecipe.id());
            correctCraftingRecipeUsed(inventory, ceRecipe);
            return;
        }
        // clear result if not met
        inventory.setResult(null);
    }

    private void correctCraftingRecipeUsed(CraftingInventory inventory, Recipe<ItemStack> recipe) {
        Object holderOrRecipe = recipeManager.getRecipeHolderByRecipe(recipe);
        if (holderOrRecipe == null) {
            // it's a vanilla recipe but not injected
            return;
        }
        try {
            Object resultInventory = Reflections.field$CraftInventoryCrafting$resultInventory.get(inventory);
            Reflections.field$ResultContainer$recipeUsed.set(resultInventory, holderOrRecipe);
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to correct used recipe", e);
        }
    }
}
