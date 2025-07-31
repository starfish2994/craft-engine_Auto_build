package net.momirealms.craftengine.bukkit.plugin.injector;

import com.mojang.datafixers.util.Pair;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRecipeTypes;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistries;
import net.momirealms.craftengine.bukkit.util.ItemTags;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CustomCookingRecipe;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.util.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class RecipeInjector {
    private static Class<?> clazz$InjectedCacheChecker;
    private static Class<?> clazz$InjectedArmorDyeRecipe;

    public static void init() {
        ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
        clazz$InjectedCacheChecker = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name("net.momirealms.craftengine.bukkit.entity.InjectedCacheChecker")
                .implement(CoreReflections.clazz$RecipeManager$CachedCheck)
                .implement(InjectedCacheCheck.class)

                .defineField("recipeType", Object.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("recipeType"))
                .intercept(FieldAccessor.ofField("recipeType"))

                .defineField("customRecipeType", Key.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("customRecipeType"))
                .intercept(FieldAccessor.ofField("customRecipeType"))

                .defineField("lastRecipe", Object.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("lastRecipe"))
                .intercept(FieldAccessor.ofField("lastRecipe"))

                .defineField("lastCustomRecipe", Key.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("lastCustomRecipe"))
                .intercept(FieldAccessor.ofField("lastCustomRecipe"))

                .method(ElementMatchers.named("getRecipeFor").or(ElementMatchers.named("a")))
                .intercept(MethodDelegation.to(
                        VersionHelper.isOrAbove1_21_2() ?
                                GetRecipeForMethodInterceptor1_21_2.INSTANCE :
                                (VersionHelper.isOrAbove1_21() ?
                                        GetRecipeForMethodInterceptor1_21.INSTANCE :
                                        VersionHelper.isOrAbove1_20_5() ?
                                                GetRecipeForMethodInterceptor1_20_5.INSTANCE :
                                                GetRecipeForMethodInterceptor1_20.INSTANCE)
                ))
                .make()
                .load(RecipeInjector.class.getClassLoader())
                .getLoaded();
        clazz$InjectedArmorDyeRecipe = byteBuddy
                .subclass(CoreReflections.clazz$ArmorDyeRecipe, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name("net.momirealms.craftengine.bukkit.item.recipe.ArmorDyeRecipe")
                .method((VersionHelper.isOrAbove1_21() ?
                        ElementMatchers.takesArguments(CoreReflections.clazz$CraftingInput, CoreReflections.clazz$Level) :
                        ElementMatchers.takesArguments(CoreReflections.clazz$CraftingContainer, CoreReflections.clazz$Level)
                ).and(ElementMatchers.returns(boolean.class)))
                .intercept(MethodDelegation.to(MatchesInterceptor.INSTANCE))
                .method((
                        VersionHelper.isOrAbove1_21() ?
                        ElementMatchers.takesArguments(CoreReflections.clazz$CraftingInput, CoreReflections.clazz$HolderLookup$Provider) :
                        VersionHelper.isOrAbove1_20_5() ?
                        ElementMatchers.takesArguments(CoreReflections.clazz$CraftingContainer, CoreReflections.clazz$HolderLookup$Provider) :
                        ElementMatchers.takesArguments(CoreReflections.clazz$CraftingContainer, CoreReflections.clazz$RegistryAccess)
                ).and(ElementMatchers.returns(CoreReflections.clazz$ItemStack)))
                .intercept(MethodDelegation.to(AssembleInterceptor.INSTANCE))
                .make()
                .load(RecipeInjector.class.getClassLoader())
                .getLoaded();
    }

    public static Object createCustomDyeRecipe() throws ReflectiveOperationException {
        if (VersionHelper.isOrAbove1_20_2()) {
            Constructor<?> constructor = ReflectionUtils.getConstructor(clazz$InjectedArmorDyeRecipe, CoreReflections.clazz$CraftingBookCategory);
            return constructor.newInstance(CoreReflections.instance$CraftingBookCategory$MISC);
        } else {
            Constructor<?> constructor = ReflectionUtils.getConstructor(clazz$InjectedArmorDyeRecipe, CoreReflections.clazz$ResourceLocation, CoreReflections.clazz$CraftingBookCategory);
            return constructor.newInstance(KeyUtils.toResourceLocation(Key.of("armor_dye")), CoreReflections.instance$CraftingBookCategory$MISC);
        }
    }

    public static void injectCookingBlockEntity(Object entity) throws ReflectiveOperationException {
        if (CoreReflections.clazz$AbstractFurnaceBlockEntity.isInstance(entity)) {
            Object quickCheck = CoreReflections.field$AbstractFurnaceBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            Object recipeType = FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$recipeType(entity);
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            if (recipeType == MRecipeTypes.SMELTING) {
                injectedChecker.customRecipeType(RecipeTypes.SMELTING);
                injectedChecker.recipeType(MRecipeTypes.SMELTING);
            } else if (recipeType == MRecipeTypes.BLASTING) {
                injectedChecker.customRecipeType(RecipeTypes.BLASTING);
                injectedChecker.recipeType(MRecipeTypes.BLASTING);
            } else if (recipeType == MRecipeTypes.SMOKING) {
                injectedChecker.customRecipeType(RecipeTypes.SMOKING);
                injectedChecker.recipeType(MRecipeTypes.SMOKING);
            } else {
                throw new IllegalStateException("RecipeType " + recipeType + " not supported");
            }
            CoreReflections.field$AbstractFurnaceBlockEntity$quickCheck.set(entity, injectedChecker);
        } else if (!VersionHelper.isOrAbove1_21_2() && CoreReflections.clazz$CampfireBlockEntity.isInstance(entity)) {
            Object quickCheck = CoreReflections.field$CampfireBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            injectedChecker.customRecipeType(RecipeTypes.CAMPFIRE_COOKING);
            injectedChecker.recipeType(MRecipeTypes.CAMPFIRE_COOKING);
            CoreReflections.field$CampfireBlockEntity$quickCheck.set(entity, injectedChecker);
        }
    }

    private static final Function<Object, Boolean> INGREDIENT_COUNT_CHECKER =
            VersionHelper.isOrAbove1_21() ?
                    (input) -> FastNMS.INSTANCE.method$CraftingInput$ingredientCount(input) < 2 :
                    (container) -> false;
    private static final Function<Object, Integer> INGREDIENT_COUNT_GETTER =
            VersionHelper.isOrAbove1_21() ?
                    FastNMS.INSTANCE::method$CraftingInput$ingredientCount :
                    FastNMS.INSTANCE::method$Container$getContainerSize;
    private static final BiFunction<Object, Integer, Object> INGREDIENT_GETTER =
            VersionHelper.isOrAbove1_21() ?
                    FastNMS.INSTANCE::method$CraftingInput$getItem :
                    FastNMS.INSTANCE::method$Container$getItem;

    public static class MatchesInterceptor {
        public static final MatchesInterceptor INSTANCE = new MatchesInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            Object input = args[0];
            if (INGREDIENT_COUNT_CHECKER.apply(input)) {
                return false;
            }
            int size = INGREDIENT_COUNT_GETTER.apply(input);
            Item<ItemStack> itemToDye = null;
            boolean hasDye = false;
            for (int i = 0; i < size; i++) {
                Object itemStack = INGREDIENT_GETTER.apply(input, i);
                if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    continue;
                }
                Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                if (isDyeable(wrapped)) {
                    if (itemToDye != null) {
                        return false;
                    }
                    itemToDye = wrapped;
                } else {
                    if (!isDye(wrapped)) {
                        return false;
                    }
                    hasDye = true;
                }
            }
            return hasDye && itemToDye != null;
        }
    }

    public static class AssembleInterceptor {
        public static final AssembleInterceptor INSTANCE = new AssembleInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            List<Color> colors = new ArrayList<>();
            Item<ItemStack> itemToDye = null;
            Object input = args[0];
            int size = INGREDIENT_COUNT_GETTER.apply(input);
            for (int i = 0; i < size; i++) {
                Object itemStack = INGREDIENT_GETTER.apply(input, i);
                if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    continue;
                }
                Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                if (isDyeable(wrapped)) {
                    itemToDye = wrapped.copyWithCount(1);
                } else {
                    Color dyeColor = getDyeColor(wrapped);
                    if (dyeColor != null) {
                        colors.add(dyeColor);
                    } else {
                        return CoreReflections.instance$ItemStack$EMPTY;
                    }
                }
            }
            if (itemToDye == null || itemToDye.isEmpty() || colors.isEmpty()) {
                return null;
            }
            return itemToDye.applyDyedColors(colors).getLiteralObject();
        }
    }

    @Nullable
    private static Color getDyeColor(final Item<ItemStack> dyeItem) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = dyeItem.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            return Optional.ofNullable(customItem.settings().dyeColor()).orElseGet(() -> getVanillaDyeColor(dyeItem));
        }
        return getVanillaDyeColor(dyeItem);
    }

    private static final Predicate<Item<ItemStack>> IS_DYEABLE =
            VersionHelper.isOrAbove1_20_5() ?
                    (item -> item.is(ItemTags.DYEABLE)) :
                    (item -> {
                       Object itemLike = FastNMS.INSTANCE.method$ItemStack$getItem(item.getLiteralObject());
                       return CoreReflections.clazz$DyeableLeatherItem.isInstance(itemLike);
                    });

    private static boolean isDyeable(final Item<ItemStack> item) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = item.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            if (customItem.settings().dyeable() == Tristate.FALSE) {
                return false;
            }
            if (customItem.settings().dyeable() == Tristate.TRUE) {
                return true;
            }
        }
        return IS_DYEABLE.test(item);
    }

    @Nullable
    private static Color getVanillaDyeColor(final Item<ItemStack> item) {
        Object itemStack = item.getLiteralObject();
        Object dyeItem = FastNMS.INSTANCE.method$ItemStack$getItem(itemStack);
        if (!CoreReflections.clazz$DyeItem.isInstance(dyeItem)) return null;
        return Color.fromDecimal(FastNMS.INSTANCE.method$DyeColor$getTextureDiffuseColor(FastNMS.INSTANCE.method$DyeItem$getDyeColor(dyeItem)));
    }

    private static boolean isDye(Item<ItemStack> dyeItem) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = dyeItem.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            return customItem.settings().dyeColor() != null || isVanillaDyeItem(dyeItem);
        }
        return isVanillaDyeItem(dyeItem);
    }

    private static boolean isVanillaDyeItem(Item<ItemStack> item) {
        return CoreReflections.clazz$DyeItem.isInstance(FastNMS.INSTANCE.method$ItemStack$getItem(item.getLiteralObject()));
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_20 {
        public static final GetRecipeForMethodInterceptor1_20 INSTANCE = new GetRecipeForMethodInterceptor1_20();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceLocation = injectedCacheCheck.lastRecipe();
            Optional<Pair<Object, Object>> optionalRecipe = FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceLocation);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            Pair<Object, Object> resourceLocationAndRecipe = optionalRecipe.get();
            Object rawRecipeResourceLocation = resourceLocationAndRecipe.getFirst();
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());
            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();

            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceLocation);
                return Optional.of(resourceLocationAndRecipe.getSecond());
            }

            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(
                    injectedCacheCheck.recipeType() == MRecipeTypes.CAMPFIRE_COOKING ?
                            FastNMS.INSTANCE.field$SimpleContainer$items(args[0]).getFirst() :
                            FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$getItem(args[0], 0)
            );

            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            if (ceRecipe == null) {
                return Optional.empty();
            }

            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(KeyUtils.toResourceLocation(ceRecipe.id()));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_20_5 {
        public static final GetRecipeForMethodInterceptor1_20_5 INSTANCE = new GetRecipeForMethodInterceptor1_20_5();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceLocation = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceLocation);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            Object rawRecipeHolder = optionalRecipe.get();
            Object rawRecipeResourceLocation = FastNMS.INSTANCE.field$RecipeHolder$id(rawRecipeHolder);
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());

            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(
                    injectedCacheCheck.recipeType() == MRecipeTypes.CAMPFIRE_COOKING ?
                            FastNMS.INSTANCE.field$SimpleContainer$items(args[0]).getFirst() :
                            FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$getItem(args[0], 0)
            );

            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceLocation);
                return optionalRecipe;
            }

            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            if (ceRecipe == null) {
                return Optional.empty();
            }

            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(KeyUtils.toResourceLocation(ceRecipe.id()));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_21 {
        public static final GetRecipeForMethodInterceptor1_21 INSTANCE = new GetRecipeForMethodInterceptor1_21();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceLocation = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceLocation);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            Object rawRecipeHolder = optionalRecipe.get();
            Object rawRecipeResourceLocation = FastNMS.INSTANCE.field$RecipeHolder$id(rawRecipeHolder);
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());

            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceLocation);
                return optionalRecipe;
            }

            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(FastNMS.INSTANCE.field$SingleRecipeInput$item(args[0]));
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            if (ceRecipe == null) {
                return Optional.empty();
            }

            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(KeyUtils.toResourceLocation(ceRecipe.id()));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_21_2 {
        public static final GetRecipeForMethodInterceptor1_21_2 INSTANCE = new GetRecipeForMethodInterceptor1_21_2();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceKey = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceKey);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            // 获取配方的基础信息
            Object recipeHolder = optionalRecipe.get();
            Object rawRecipeResourceKey = FastNMS.INSTANCE.field$RecipeHolder$id(recipeHolder);
            Object rawRecipeResourceLocation = FastNMS.INSTANCE.field$ResourceKey$location(rawRecipeResourceKey);
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());

            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
            // 来自其他插件注册的自定义配方
            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceKey);
                return optionalRecipe;
            }

            // 获取唯一内存地址id
            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(FastNMS.INSTANCE.field$SingleRecipeInput$item(args[0]));
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            // 这个ce配方并不存在，那么应该返回空
            if (ceRecipe == null) {
                return Optional.empty();
            }

            // 记录上一次使用的配方(ce)
            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            // 更新上一次使用的配方(nms)
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(FastNMS.INSTANCE.method$ResourceKey$create(MRegistries.RECIPE, KeyUtils.toResourceLocation(ceRecipe.id())));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }
}
