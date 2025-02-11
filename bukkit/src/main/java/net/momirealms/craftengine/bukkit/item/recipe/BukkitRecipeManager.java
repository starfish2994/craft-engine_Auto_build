package net.momirealms.craftengine.bukkit.item.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagCompound;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.CloneableConstantItem;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.MaterialUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.vanilla.RecipeResult;
import net.momirealms.craftengine.core.item.recipe.vanilla.VanillaRecipeReader;
import net.momirealms.craftengine.core.item.recipe.vanilla.VanillaShapedRecipe;
import net.momirealms.craftengine.core.item.recipe.vanilla.VanillaShapelessRecipe;
import net.momirealms.craftengine.core.item.recipe.vanilla.impl.VanillaRecipeReader1_20;
import net.momirealms.craftengine.core.item.recipe.vanilla.impl.VanillaRecipeReader1_20_5;
import net.momirealms.craftengine.core.item.recipe.vanilla.impl.VanillaRecipeReader1_21_2;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class BukkitRecipeManager implements RecipeManager<ItemStack> {
    private static final Map<Key, BiFunction<NamespacedKey, Recipe<ItemStack>, Object>> BUKKIT_RECIPE_CONVERTORS = new HashMap<>();

    static {
        BUKKIT_RECIPE_CONVERTORS.put(RecipeTypes.SHAPED, (key, recipe) -> {
            CustomShapedRecipe<ItemStack> ceRecipe = (CustomShapedRecipe<ItemStack>) recipe;
            ShapedRecipe shapedRecipe = new ShapedRecipe(key, new ItemStack(Material.STONE));
            if (ceRecipe.group() != null) {
                shapedRecipe.setGroup(Objects.requireNonNull(ceRecipe.group()));
            }
            if (ceRecipe.category() != null) {
                shapedRecipe.setCategory(CraftingBookCategory.valueOf(Objects.requireNonNull(ceRecipe.category()).name()));
            }
            shapedRecipe.shape(ceRecipe.pattern().pattern());
            for (Map.Entry<Character, Ingredient<ItemStack>> entry : ceRecipe.pattern().ingredients().entrySet()) {
                shapedRecipe.setIngredient(entry.getKey(), new RecipeChoice.MaterialChoice(ingredientToBukkitMaterials(entry.getValue())));
            }
            try {
                return Reflections.method$CraftShapedRecipe$fromBukkitRecipe.invoke(null, shapedRecipe);
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to convert shaped recipe", e);
                return null;
            }
        });
        BUKKIT_RECIPE_CONVERTORS.put(RecipeTypes.SHAPELESS, (key, recipe) -> {
            CustomShapelessRecipe<ItemStack> ceRecipe = (CustomShapelessRecipe<ItemStack>) recipe;
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, new ItemStack(Material.STONE));
            if (ceRecipe.group() != null) {
                shapelessRecipe.setGroup(Objects.requireNonNull(ceRecipe.group()));
            }
            if (ceRecipe.category() != null) {
                shapelessRecipe.setCategory(CraftingBookCategory.valueOf(Objects.requireNonNull(ceRecipe.category()).name()));
            }
            for (Ingredient<ItemStack> ingredient : ceRecipe.ingredients()) {
                shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(ingredientToBukkitMaterials(ingredient)));
            }
            try {
                return Reflections.method$CraftShapelessRecipe$fromBukkitRecipe.invoke(null, shapelessRecipe);
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to convert shapeless recipe", e);
                return null;
            }
        });
    }

    private final BukkitCraftEngine plugin;
    private final RecipeEventListener recipeEventListener;
    private final Map<Key, List<Recipe<ItemStack>>> recipes;
    private final VanillaRecipeReader recipeReader;
    private final List<NamespacedKey> injectedDataPackRecipes;
    private final List<NamespacedKey> registeredCustomRecipes;
    // [internal:xxx]   +   [custom:custom]
    // includes injected vanilla recipes and custom recipes
    private final Set<Key> customRecipes;
    // data pack recipe resource locations [minecraft:xxx]
    private final Set<Key> dataPackRecipes;

    private Object stolenFeatureFlagSet;
    private Object minecraftRecipeManager;

    public BukkitRecipeManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        this.recipeEventListener = new RecipeEventListener(plugin, this, plugin.itemManager());
        this.recipes = new HashMap<>();
        this.injectedDataPackRecipes = new ArrayList<>();
        this.registeredCustomRecipes = new ArrayList<>();
        this.dataPackRecipes = new HashSet<>();
        this.customRecipes = new HashSet<>();
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            this.recipeReader = new VanillaRecipeReader1_21_2();
        } else if (VersionHelper.isVersionNewerThan1_20_5()) {
            this.recipeReader = new VanillaRecipeReader1_20_5();
        } else {
            this.recipeReader = new VanillaRecipeReader1_20();
        }
        try {
            this.minecraftRecipeManager = Reflections.method$MinecraftServer$getRecipeManager.invoke(Reflections.method$MinecraftServer$getServer.invoke(null));
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to get minecraft recipe manager", e);
        }
    }

    @Override
    public boolean isDataPackRecipe(Key key) {
        return this.dataPackRecipes.contains(key);
    }

    @Override
    public boolean isCustomRecipe(Key key) {
        return this.customRecipes.contains(key);
    }

    @Override
    public void load() {
        if (!ConfigManager.enableRecipeSystem()) return;
        Bukkit.getPluginManager().registerEvents(this.recipeEventListener, this.plugin.bootstrap());
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            try {
                this.stolenFeatureFlagSet = Reflections.field$RecipeManager$featureflagset.get(this.minecraftRecipeManager);
                Reflections.field$RecipeManager$featureflagset.set(this.minecraftRecipeManager, null);
            } catch (ReflectiveOperationException e) {
                this.plugin.logger().warn("Failed to steal featureflagset", e);
            }
        }
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.recipeEventListener);
        this.recipes.clear();
        this.dataPackRecipes.clear();
        this.customRecipes.clear();
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            try {
                Object recipeMap = Reflections.field$RecipeManager$recipes.get(this.minecraftRecipeManager);
                for (NamespacedKey key : this.injectedDataPackRecipes) {
                    Reflections.method$RecipeMap$removeRecipe.invoke(recipeMap, Reflections.method$CraftRecipe$toMinecraft.invoke(null, key));
                }
                for (NamespacedKey key : this.registeredCustomRecipes) {
                    Reflections.method$RecipeMap$removeRecipe.invoke(recipeMap, Reflections.method$CraftRecipe$toMinecraft.invoke(null, key));
                }
                Reflections.method$RecipeManager$finalizeRecipeLoading.invoke(this.minecraftRecipeManager);
            } catch (ReflectiveOperationException e) {
                plugin.logger().warn("Failed to unload custom recipes", e);
            }
        } else {
            for (NamespacedKey key : this.injectedDataPackRecipes) {
                Bukkit.removeRecipe(key);
            }
            for (NamespacedKey key : this.registeredCustomRecipes) {
                Bukkit.removeRecipe(key);
            }
        }
        this.registeredCustomRecipes.clear();
        this.injectedDataPackRecipes.clear();
    }

    @Override
    public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
        if (!ConfigManager.enableRecipeSystem()) return;
        if (this.customRecipes.contains(id)) {
            this.plugin.logger().warn(path, "Duplicated recipe " + id);
            return;
        }
        Recipe<ItemStack> recipe = RecipeTypes.fromMap(section);
        NamespacedKey key = NamespacedKey.fromString(id.toString());
        Object craftRecipe = BUKKIT_RECIPE_CONVERTORS.get(recipe.type()).apply(key, recipe);
        try {
            // to bypass paper's "resend"
            Reflections.method$CraftRecipe$addToCraftingManager.invoke(craftRecipe);
            this.registeredCustomRecipes.add(key);
            this.customRecipes.add(id);
            this.recipes.computeIfAbsent(recipe.type(), k -> new ArrayList<>()).add(recipe);
        } catch (Exception e) {
            plugin.logger().warn("Failed to add custom recipe " + id, e);
        }
    }

    @Override
    public List<Recipe<ItemStack>> getRecipes(Key type) {
        return this.recipes.getOrDefault(type, List.of());
    }

    public void addVanillaInternalRecipe(Key id, Recipe<ItemStack> recipe) {
        this.customRecipes.add(id);
        this.recipes.computeIfAbsent(recipe.type(), k -> new ArrayList<>()).add(recipe);
    }

    @Nullable
    @Override
    public Recipe<ItemStack> getRecipe(Key type, RecipeInput input) {
        List<Recipe<ItemStack>> recipes = this.recipes.get(type);
        if (recipes == null) return null;
        for (Recipe<ItemStack> recipe : recipes) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public void delayedLoad() {
        this.processVanillaRecipes().thenRun(() -> {
            if (VersionHelper.isVersionNewerThan1_21_2() && this.stolenFeatureFlagSet != null) {
                try {
                    Reflections.field$RecipeManager$featureflagset.set(this.minecraftRecipeManager, this.stolenFeatureFlagSet);
                    this.stolenFeatureFlagSet = false;
                    Reflections.method$RecipeManager$finalizeRecipeLoading.invoke(this.minecraftRecipeManager);
                } catch (ReflectiveOperationException e) {
                    this.plugin.logger().warn("Failed to give featureflagset back", e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> processVanillaRecipes() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            List<Object> bukkitRecipesToRegister = new ArrayList<>();
            plugin.scheduler().async().execute(() -> {
                try {
                    Object fileToIdConverter = Reflections.method$FileToIdConverter$json.invoke(null, VersionHelper.isVersionNewerThan1_21() ? "recipe" : "recipes");
                    Object minecraftServer = Reflections.method$MinecraftServer$getServer.invoke(null);
                    Object packRepository = Reflections.method$MinecraftServer$getPackRepository.invoke(minecraftServer);
                    List<Object> selected = (List<Object>) Reflections.field$PackRepository$selected.get(packRepository);
                    List<Object> packResources = new ArrayList<>();
                    for (Object pack : selected) {
                        packResources.add(Reflections.method$Pack$open.invoke(pack));
                    }
                    try (AutoCloseable resourceManager = (AutoCloseable) Reflections.constructor$MultiPackResourceManager.newInstance(Reflections.instance$PackType$SERVER_DATA, packResources)) {
                        Map<Object, Object> scannedResources = (Map<Object, Object>) Reflections.method$FileToIdConverter$listMatchingResources.invoke(fileToIdConverter, resourceManager);
                        for (Map.Entry<Object, Object> entry : scannedResources.entrySet()) {
                            Key id = extractKeyFromResourceLocation(entry.getKey().toString());
                            this.dataPackRecipes.add(id);
                            // Maybe it's unregistered by other plugins
                            if (Bukkit.getRecipe(new NamespacedKey(id.namespace(), id.value())) == null) {
                                continue;
                            }
                            Reader reader = (Reader) Reflections.method$Resource$openAsReader.invoke(entry.getValue());
                            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                            String type = jsonObject.get("type").getAsString();
                            switch (type) {
                                case "minecraft:crafting_shaped" -> {
                                    VanillaShapedRecipe recipe = this.recipeReader.readShaped(jsonObject);
                                    handleDataPackShapedRecipe(id, recipe, (shapedRecipe -> {
                                        try {
                                            bukkitRecipesToRegister.add(Reflections.method$CraftShapedRecipe$fromBukkitRecipe.invoke(null, shapedRecipe));
                                        } catch (Exception e) {
                                            CraftEngine.instance().logger().warn("Failed to convert shaped recipe", e);
                                        }
                                    }));
                                }
                                case "minecraft:crafting_shapeless" -> {
                                    VanillaShapelessRecipe recipe = this.recipeReader.readShapeless(jsonObject);
                                    handleDataPackShapelessRecipe(id, recipe, (shapelessRecipe -> {
                                        try {
                                            bukkitRecipesToRegister.add(Reflections.method$CraftShapelessRecipe$fromBukkitRecipe.invoke(null, shapelessRecipe));
                                        } catch (Exception e) {
                                            CraftEngine.instance().logger().warn("Failed to convert shapeless recipe", e);
                                        }
                                    }));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.logger().warn("Failed to read data pack recipes", e);
                } finally {
                    plugin.scheduler().sync().run(() -> {
                        try {
                            for (Object recipe : bukkitRecipesToRegister) {
                                Reflections.method$CraftRecipe$addToCraftingManager.invoke(recipe);
                            }
                        } catch (Exception e) {
                            CraftEngine.instance().logger().warn("Failed to register recipes", e);
                        } finally {
                            future.complete(null);
                        }
                    });
                }
            });
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to inject vanilla recipes", e);
        }
        return future;
    }

    private void handleDataPackShapelessRecipe(Key id, VanillaShapelessRecipe recipe, Consumer<org.bukkit.inventory.ShapelessRecipe> callback) {
        NamespacedKey key = new NamespacedKey("internal", id.value());
        ItemStack result = createResultStack(recipe.result());
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);
        if (recipe.group() != null) {
            shapelessRecipe.setGroup(recipe.group());
        }
        if (recipe.category() != null) {
            shapelessRecipe.setCategory(CraftingBookCategory.valueOf(recipe.category().name()));
        }

        boolean hasCustomItemInTag = false;
        List<Ingredient<ItemStack>> ingredientList = new ArrayList<>();
        for (List<String> list : recipe.ingredients()) {
            Set<Material> materials = new HashSet<>();
            Set<Holder<Key>> holders = new HashSet<>();
            for (String item : list) {
                if (item.charAt(0) == '#') {
                    Key tag = Key.of(item.substring(1));
                    materials.addAll(tagToMaterials(tag));
                    if (!hasCustomItemInTag) {
                        if (!plugin.itemManager().tagToCustomItems(tag).isEmpty()) {
                            hasCustomItemInTag = true;
                        }
                    }
                    holders.addAll(plugin.itemManager().tagToItems(tag));
                } else {
                    materials.add(MaterialUtils.getMaterial(item));
                    holders.add(BuiltInRegistries.OPTIMIZED_ITEM_ID.get(Key.from(item)).orElseThrow());
                }
            }
            shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(new ArrayList<>(materials)));
            ingredientList.add(Ingredient.of(holders));
        }

        if (hasCustomItemInTag) {
            callback.accept(shapelessRecipe);
            this.injectedDataPackRecipes.add(key);
        }
        CustomShapelessRecipe<ItemStack> ceRecipe = new CustomShapelessRecipe<>(
                recipe.category(),
                recipe.group(),
                ingredientList,
                new CustomRecipeResult<>(new CloneableConstantItem(result), recipe.result().count())
        );
        this.addVanillaInternalRecipe(Key.of("internal", id.value()), ceRecipe);
    }

    private void handleDataPackShapedRecipe(Key id, VanillaShapedRecipe recipe, Consumer<org.bukkit.inventory.ShapedRecipe> callback) {
        NamespacedKey key = new NamespacedKey("internal", id.value());
        ItemStack result = createResultStack(recipe.result());
        ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
        if (recipe.group() != null) {
            shapedRecipe.setGroup(recipe.group());
        }
        if (recipe.category() != null) {
            shapedRecipe.setCategory(CraftingBookCategory.valueOf(recipe.category().name()));
        }
        shapedRecipe.shape(recipe.pattern());

        boolean hasCustomItemInTag = false;
        Map<Character, Ingredient<ItemStack>> ingredients = new HashMap<>();
        for (Map.Entry<Character, List<String>> entry : recipe.ingredients().entrySet()) {
            Set<Material> materials = new HashSet<>();
            Set<Holder<Key>> holders = new HashSet<>();
            for (String item : entry.getValue()) {
                if (item.charAt(0) == '#') {
                    Key tag = Key.from(item.substring(1));
                    materials.addAll(tagToMaterials(tag));
                    if (!hasCustomItemInTag) {
                        if (!plugin.itemManager().tagToCustomItems(tag).isEmpty()) {
                            hasCustomItemInTag = true;
                        }
                    }
                    holders.addAll(plugin.itemManager().tagToItems(tag));
                } else {
                    materials.add(MaterialUtils.getMaterial(item));
                    holders.add(BuiltInRegistries.OPTIMIZED_ITEM_ID.get(Key.from(item)).orElseThrow());
                }
            }
            shapedRecipe.setIngredient(entry.getKey(), new RecipeChoice.MaterialChoice(new ArrayList<>(materials)));
            ingredients.put(entry.getKey(), Ingredient.of(holders));
        }

        if (hasCustomItemInTag) {
            callback.accept(shapedRecipe);
            this.injectedDataPackRecipes.add(key);
        }
        CustomShapedRecipe<ItemStack> ceRecipe = new CustomShapedRecipe<>(
                recipe.category(),
                recipe.group(),
                new CustomShapedRecipe.Pattern<>(recipe.pattern(), ingredients),
                new CustomRecipeResult<>(new CloneableConstantItem(result), recipe.result().count())
        );
        this.addVanillaInternalRecipe(Key.of("internal", id.value()), ceRecipe);
    }

    private List<Material> tagToMaterials(Key tag) {
        Set<Material> materials = new HashSet<>();
        List<Holder<Key>> holders = this.plugin.itemManager().tagToVanillaItems(tag);
        if (holders != null) {
            for (Holder<Key> holder : holders) {
                materials.add(MaterialUtils.getMaterial(holder.value()));
            }
        }
        List<Holder<Key>> customItems = this.plugin.itemManager().tagToCustomItems(tag);
        if (customItems != null) {
            for (Holder<Key> holder : customItems) {
                this.plugin.itemManager().getCustomItem(holder.value()).ifPresent(it -> {
                    materials.add(MaterialUtils.getMaterial(it.material()));
                });
            }
        }
        return new ArrayList<>(materials);
    }

    private ItemStack createResultStack(RecipeResult result) {
        ItemStack itemStack;
        if (result.components() == null) {
            itemStack = new ItemStack(Objects.requireNonNull(MaterialUtils.getMaterial(result.id())));
            itemStack.setAmount(result.count());
        } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", result.id());
            jsonObject.addProperty("count", result.count());
            jsonObject.add("components", result.components());
            Object nmsStack = ItemObject.newItem(TagCompound.newTag(jsonObject.toString()));
            try {
                itemStack = (ItemStack) Reflections.method$CraftItemStack$asCraftMirror.invoke(null, nmsStack);
            } catch (Exception e) {
                this.plugin.logger().warn("Failed to create ItemStack mirror", e);
                return new ItemStack(Material.STICK);
            }
        }
        return itemStack;
    }

    private Key extractKeyFromResourceLocation(String input) {
        int prefixEndIndex = input.indexOf(':');
        String prefix = input.substring(0, prefixEndIndex);
        int lastSlashIndex = input.lastIndexOf('/');
        int lastDotIndex = input.lastIndexOf('.');
        String fileName = input.substring(lastSlashIndex + 1, lastDotIndex);
        return Key.of(prefix, fileName);
    }

    private static List<Material> ingredientToBukkitMaterials(Ingredient<ItemStack> ingredient) {
        Set<Material> materials = new HashSet<>();
        for (Holder<Key> holder : ingredient.items()) {
            materials.add(getMaterialById(holder.value()));
        }
        return new ArrayList<>(materials);
    }

    private static Material getMaterialById(Key key) {
        Material material = MaterialUtils.getMaterial(key);
        if (material != null) {
            return material;
        }
        Optional<CustomItem<ItemStack>> optionalItem = BukkitItemManager.instance().getCustomItem(key);
        return optionalItem.map(itemStackCustomItem -> MaterialUtils.getMaterial(itemStackCustomItem.material())).orElse(null);
    }
}
