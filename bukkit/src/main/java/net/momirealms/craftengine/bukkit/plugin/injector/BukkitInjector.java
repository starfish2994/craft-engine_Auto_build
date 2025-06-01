package net.momirealms.craftengine.bukkit.plugin.injector;

import com.mojang.datafixers.util.Pair;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRecipeTypes;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.StatePredicate;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CustomCookingRecipe;
import net.momirealms.craftengine.core.item.recipe.OptimizedIDItem;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.SectionPosUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BukkitInjector {

    private static Class<?> clazz$InjectedPalettedContainer;
    private static Class<?> clazz$InjectedLevelChunkSection;
    private static MethodHandle constructor$InjectedLevelChunkSection;
    private static VarHandle varHandle$InjectedPalettedContainer$target;
    private static Class<?> clazz$OptimizedItemDisplay;
    private static Constructor<?> constructor$OptimizedItemDisplay;
    private static Class<?> clazz$OptimizedItemDisplayFatory;
    private static Object instance$OptimizedItemDisplayFactory;
    private static Class<?> clazz$InjectedCacheChecker;

    private static InternalFieldAccessor internalFieldAccessor;

    public static void init() {
        try {
            ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
            // Paletted Container
            clazz$InjectedPalettedContainer = byteBuddy
                    .subclass(CoreReflections.clazz$PalettedContainer)
                    .name("net.minecraft.world.level.chunk.InjectedPalettedContainer")
                    .implement(InjectedHolder.Palette.class)
                    .defineField("target", CoreReflections.clazz$PalettedContainer, Visibility.PUBLIC)
                    .defineField("active", boolean.class, Visibility.PUBLIC)
                    .defineField("cesection", CESection.class, Visibility.PRIVATE)
                    .defineField("cechunk", CEChunk.class, Visibility.PRIVATE)
                    .defineField("cepos", SectionPos.class, Visibility.PRIVATE)
                    .method(ElementMatchers.any()
                            .and(ElementMatchers.not(ElementMatchers.is(CoreReflections.method$PalettedContainer$getAndSet)))
                            .and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                    )
                    .intercept(MethodDelegation.toField("target"))
                    .method(ElementMatchers.is(CoreReflections.method$PalettedContainer$getAndSet))
                    .intercept(MethodDelegation.to(GetAndSetInterceptor.INSTANCE))
                    .method(ElementMatchers.named("target"))
                    .intercept(FieldAccessor.ofField("target"))
                    .method(ElementMatchers.named("setTarget"))
                    .intercept(FieldAccessor.ofField("target").withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .method(ElementMatchers.named("isActive").or(ElementMatchers.named("setActive")))
                    .intercept(FieldAccessor.ofField("active"))
                    .method(ElementMatchers.named("ceSection"))
                    .intercept(FieldAccessor.ofField("cesection"))
                    .method(ElementMatchers.named("ceChunk"))
                    .intercept(FieldAccessor.ofField("cechunk"))
                    .method(ElementMatchers.named("cePos"))
                    .intercept(FieldAccessor.ofField("cepos"))
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
            //varHandle$InjectedPalettedContainer$target = Objects.requireNonNull(ReflectionUtils.findVarHandle(clazz$InjectedPalettedContainer, "target", CoreReflections.clazz$PalettedContainer));

            // Level Chunk Section
            clazz$InjectedLevelChunkSection = byteBuddy
                    .subclass(CoreReflections.clazz$LevelChunkSection, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.minecraft.world.level.chunk.InjectedLevelChunkSection")
                    .implement(InjectedHolder.Section.class)
                    .defineField("active", boolean.class, Visibility.PUBLIC)
                    .defineField("cesection", CESection.class, Visibility.PRIVATE)
                    .defineField("cechunk", CEChunk.class, Visibility.PRIVATE)
                    .defineField("cepos", SectionPos.class, Visibility.PRIVATE)
                    .method(ElementMatchers.is(CoreReflections.method$LevelChunkSection$setBlockState))
                        .intercept(MethodDelegation.to(SetBlockStateInterceptor.INSTANCE))
                    .method(ElementMatchers.named("ceSection"))
                    .intercept(FieldAccessor.ofField("cesection"))
                    .method(ElementMatchers.named("ceChunk"))
                    .intercept(FieldAccessor.ofField("cechunk"))
                    .method(ElementMatchers.named("cePos"))
                    .intercept(FieldAccessor.ofField("cepos"))
                    .method(ElementMatchers.named("isActive").or(ElementMatchers.named("setActive")))
                    .intercept(FieldAccessor.ofField("active"))
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();

            constructor$InjectedLevelChunkSection = MethodHandles.publicLookup().in(clazz$InjectedLevelChunkSection)
                    .findConstructor(clazz$InjectedLevelChunkSection, MethodType.methodType(void.class, CoreReflections.clazz$PalettedContainer, CoreReflections.clazz$PalettedContainer))
                    .asType(MethodType.methodType(CoreReflections.clazz$LevelChunkSection, CoreReflections.clazz$PalettedContainer, CoreReflections.clazz$PalettedContainer));

            // State Predicate
            DynamicType.Unloaded<?> alwaysTrue = byteBuddy
                    .subclass(CoreReflections.clazz$StatePredicate)
                    .method(ElementMatchers.named("test"))
                    .intercept(FixedValue.value(true))
                    .make();
            Class<?> alwaysTrueClass = alwaysTrue.load(BukkitInjector.class.getClassLoader()).getLoaded();
            DynamicType.Unloaded<?> alwaysFalse = byteBuddy
                    .subclass(CoreReflections.clazz$StatePredicate)
                    .method(ElementMatchers.named("test"))
                    .intercept(FixedValue.value(false))
                    .make();
            Class<?> alwaysFalseClass = alwaysFalse.load(BukkitInjector.class.getClassLoader()).getLoaded();
            StatePredicate.init(alwaysTrueClass.getDeclaredConstructor().newInstance(), alwaysFalseClass.getDeclaredConstructor().newInstance());
            // Optimized Item Display
            clazz$OptimizedItemDisplay = byteBuddy
                    .subclass(CoreReflections.clazz$Display$ItemDisplay, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.minecraft.world.entity.OptimizedItemDisplay")
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
            constructor$OptimizedItemDisplay = ReflectionUtils.getConstructor(clazz$OptimizedItemDisplay, CoreReflections.clazz$EntityType, CoreReflections.clazz$Level);
            clazz$OptimizedItemDisplayFatory = byteBuddy
                    .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.momirealms.craftengine.bukkit.entity.OptimizedItemDisplayFactory")
                    .implement(CoreReflections.clazz$EntityType$EntityFactory)
                    .method(ElementMatchers.named("create"))
                    .intercept(MethodDelegation.to(OptimizedItemDisplayMethodInterceptor.INSTANCE))
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
            instance$OptimizedItemDisplayFactory = Objects.requireNonNull(ReflectionUtils.getConstructor(clazz$OptimizedItemDisplayFatory, 0)).newInstance();

            // InternalFieldAccessor Interface
            Class<?> internalFieldAccessorInterface = new ByteBuddy()
                    .makeInterface()
                    .name("net.momirealms.craftengine.bukkit.plugin.injector.InternalFieldAccessor")
                    .defineMethod("field$ClientboundMoveEntityPacket$entityId", int.class, Modifier.PUBLIC)
                    .withParameter(Object.class, "packet")
                    .withoutCode()
                    .make()
                    .load(NetworkReflections.clazz$ClientboundMoveEntityPacket.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();

            // Internal field accessor
            FieldDescription moveEntityIdFieldDesc = new FieldDescription.ForLoadedField(NetworkReflections.field$ClientboundMoveEntityPacket$entityId);
            Class<?> clazz$InternalFieldAccessor = byteBuddy
                    .subclass(Object.class)
                    .name("net.minecraft.network.protocol.game.CraftEngineInternalFieldAccessor")
                    .implement(internalFieldAccessorInterface)
                    .method(ElementMatchers.named("field$ClientboundMoveEntityPacket$entityId"))
                    .intercept(new Implementation.Simple(
                            MethodVariableAccess.REFERENCE.loadFrom(1),
                            TypeCasting.to(TypeDescription.ForLoadedType.of(NetworkReflections.clazz$ClientboundMoveEntityPacket)),
                            FieldAccess.forField(moveEntityIdFieldDesc).read(),
                            MethodReturn.INTEGER
                    ))
                    .make()
                    .load(NetworkReflections.clazz$ClientboundMoveEntityPacket.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
            internalFieldAccessor = (InternalFieldAccessor) clazz$InternalFieldAccessor.getConstructor().newInstance();



            clazz$InjectedCacheChecker = byteBuddy
                    .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.momirealms.craftengine.bukkit.entity.InjectedCacheChecker")
                    .implement(CoreReflections.clazz$RecipeManager$CachedCheck)
                    .implement(InjectedCacheCheck.class)
                    .defineField("recipeType", Object.class, Visibility.PUBLIC)
                    .method(ElementMatchers.named("recipeType"))
                    .intercept(FieldAccessor.ofField("recipeType"))
                    .defineField("lastRecipe", Object.class, Visibility.PUBLIC)
                    .method(ElementMatchers.named("lastRecipe"))
                    .intercept(FieldAccessor.ofField("lastRecipe"))
                    .method(ElementMatchers.named("setLastRecipe"))
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
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
        } catch (Throwable e) {
            CraftEngine.instance().logger().severe("Failed to init injector", e);
        }
    }

    public static InternalFieldAccessor internalFieldAccessor() {
        return internalFieldAccessor;
    }

    public static void injectCookingBlockEntity(Object entity) throws ReflectiveOperationException {
        if (CoreReflections.clazz$AbstractFurnaceBlockEntity.isInstance(entity)) {
            Object quickCheck = CoreReflections.field$AbstractFurnaceBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            Object recipeType = FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$recipeType(entity);
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            injectedChecker.recipeType(recipeType);
            CoreReflections.field$AbstractFurnaceBlockEntity$quickCheck.set(entity, injectedChecker);
        } else if (!VersionHelper.isOrAbove1_21_2() && CoreReflections.clazz$CampfireBlockEntity.isInstance(entity)) {
            Object quickCheck = CoreReflections.field$CampfireBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            injectedChecker.recipeType(MRecipeTypes.CAMPFIRE_COOKING);
            CoreReflections.field$CampfireBlockEntity$quickCheck.set(entity, injectedChecker);
        }
    }

    public static Object getOptimizedItemDisplayFactory() {
        return instance$OptimizedItemDisplayFactory;
    }

    public static class OptimizedItemDisplayMethodInterceptor {
        public static final OptimizedItemDisplayMethodInterceptor INSTANCE = new OptimizedItemDisplayMethodInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) throws Exception {
            return constructor$OptimizedItemDisplay.newInstance(args[0], args[1]);
        }
    }

//    public synchronized static void injectLevelChunkSection(Object targetSection, CESection ceSection, CEWorld ceWorld, SectionPos pos) {
//        try {
//            Object container = FastNMS.INSTANCE.field$LevelChunkSection$states(targetSection);
//            if (!(container instanceof InjectedPalettedContainerHolder)) {
//                InjectedPalettedContainerHolder injectedObject = FastNMS.INSTANCE.createInjectedPalettedContainerHolder(container);
//                injectedObject.ceSection(ceSection);
//                injectedObject.ceWorld(ceWorld);
//                injectedObject.cePos(pos);
//                CoreReflections.varHandle$PalettedContainer$data.setVolatile(injectedObject, CoreReflections.varHandle$PalettedContainer$data.get(container));
//                CoreReflections.field$LevelChunkSection$states.set(targetSection, injectedObject);
//            }
//        } catch (Exception e) {
//            CraftEngine.instance().logger().severe("Failed to inject chunk section", e);
//        }
//    }

    public synchronized static void injectLevelChunkSection(Object targetSection, CESection ceSection, CEChunk chunk, SectionPos pos, Consumer<Object> callback) {
        try {
            if (Config.injectionTarget()) {
                Object container = FastNMS.INSTANCE.field$LevelChunkSection$states(targetSection);
                if (!(container instanceof InjectedHolder.Palette holder)) {
                    InjectedHolder.Palette injectedObject;
                    if (Config.fastInjection()) {
                        injectedObject = FastNMS.INSTANCE.createInjectedPalettedContainerHolder(container);
                    } else {
                        injectedObject = (InjectedHolder.Palette) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedPalettedContainer);
                        injectedObject.setTarget(container);
                        //varHandle$InjectedPalettedContainer$target.set(injectedObject, container);
                    }
                    injectedObject.ceChunk(chunk);
                    injectedObject.ceSection(ceSection);
                    injectedObject.cePos(pos);
                    injectedObject.setActive(true);
                    CoreReflections.varHandle$PalettedContainer$data.setVolatile(injectedObject, CoreReflections.varHandle$PalettedContainer$data.get(container));
                    CoreReflections.field$LevelChunkSection$states.set(targetSection, injectedObject);
                } else {
                    holder.ceChunk(chunk);
                    holder.ceSection(ceSection);
                    holder.cePos(pos);
                    holder.setActive(true);
                }
            } else {
                if (!(targetSection instanceof InjectedHolder.Section holder)) {
                    InjectedHolder.Section injectedObject;
                    if (Config.fastInjection()) {
                        injectedObject = FastNMS.INSTANCE.createInjectedLevelChunkSectionHolder(targetSection);
                    } else {
                        injectedObject = (InjectedHolder.Section) constructor$InjectedLevelChunkSection.invoke(
                                FastNMS.INSTANCE.field$LevelChunkSection$states(targetSection), FastNMS.INSTANCE.field$LevelChunkSection$biomes(targetSection));
                    }
                    injectedObject.ceChunk(chunk);
                    injectedObject.ceSection(ceSection);
                    injectedObject.cePos(pos);
                    injectedObject.setActive(true);
                    callback.accept(injectedObject);
                } else {
                    holder.ceChunk(chunk);
                    holder.ceSection(ceSection);
                    holder.cePos(pos);
                    holder.setActive(true);
                }
            }
        } catch (Throwable e) {
            CraftEngine.instance().logger().severe("Failed to inject chunk section " + pos, e);
        }
    }

    public static boolean isSectionInjected(Object section) {
        if (Config.injectionTarget()) {
            Object container = FastNMS.INSTANCE.field$LevelChunkSection$states(section);
            return container instanceof InjectedHolder.Palette;
        } else {
            return section instanceof InjectedHolder.Section;
        }
    }

    public synchronized static Object uninjectLevelChunkSection(Object section) {
        if (Config.injectionTarget()) {
            Object states = FastNMS.INSTANCE.field$LevelChunkSection$states(section);
            if (states instanceof InjectedHolder.Palette holder) {
                holder.setActive(false);
//                try {
//                    CoreReflections.field$LevelChunkSection$states.set(section, holder.target());
//                } catch (ReflectiveOperationException e) {
//                    CraftEngine.instance().logger().severe("Failed to uninject palette", e);
//                }
            }
        } else {
            if (section instanceof InjectedHolder.Section holder) {
                holder.setActive(false);
                //return FastNMS.INSTANCE.constructor$LevelChunkSection(holder);
            }
        }
        return section;
    }

    public static class GetRecipeForMethodInterceptor1_20 {
        public static final GetRecipeForMethodInterceptor1_20 INSTANCE = new GetRecipeForMethodInterceptor1_20();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Pair<Object, Object>> optionalRecipe = FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Pair<Object, Object> pair = optionalRecipe.get();
                Object resourceLocation = pair.getFirst();
                Key recipeId = Key.of(resourceLocation.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();

                ItemStack itemStack;
                List<Object> items;
                if (type == MRecipeTypes.CAMPFIRE_COOKING) {
                    items = (List<Object>) CoreReflections.field$SimpleContainer$items.get(args[0]);
                } else {
                    items = (List<Object>) CoreReflections.field$AbstractFurnaceBlockEntity$items.get(args[0]);
                }
                itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(items.get(0));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(resourceLocation);
                    return Optional.of(pair.getSecond());
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == MRecipeTypes.SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.CAMPFIRE_COOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.CAMPFIRE_COOKING, input, lastCustomRecipe);
                } else  {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(resourceLocation);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(pair.getSecond()));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetRecipeForMethodInterceptor1_20_5 {
        public static final GetRecipeForMethodInterceptor1_20_5 INSTANCE = new GetRecipeForMethodInterceptor1_20_5();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Object holder = optionalRecipe.get();
                Object id = FastNMS.INSTANCE.field$RecipeHolder$id(holder);
                Key recipeId = Key.of(id.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();

                ItemStack itemStack;
                List<Object> items;
                if (type == MRecipeTypes.CAMPFIRE_COOKING) {
                    items = (List<Object>) CoreReflections.field$SimpleContainer$items.get(args[0]);
                } else {
                    items = (List<Object>) CoreReflections.field$AbstractFurnaceBlockEntity$items.get(args[0]);
                }
                itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(items.get(0));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(id);
                    return optionalRecipe;
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == MRecipeTypes.SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.CAMPFIRE_COOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.CAMPFIRE_COOKING, input, lastCustomRecipe);
                } else  {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(id);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(holder));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetRecipeForMethodInterceptor1_21 {
        public static final GetRecipeForMethodInterceptor1_21 INSTANCE = new GetRecipeForMethodInterceptor1_21();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Object holder = optionalRecipe.get();
                Object id = FastNMS.INSTANCE.field$RecipeHolder$id(holder);
                Key recipeId = Key.of(id.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
                ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(CoreReflections.field$SingleRecipeInput$item.get(args[0]));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(id);
                    return optionalRecipe;
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == MRecipeTypes.SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.CAMPFIRE_COOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.CAMPFIRE_COOKING, input, lastCustomRecipe);
                } else  {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(id);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(holder));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetRecipeForMethodInterceptor1_21_2 {
        public static final GetRecipeForMethodInterceptor1_21_2 INSTANCE = new GetRecipeForMethodInterceptor1_21_2();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Object holder = optionalRecipe.get();
                Object id = FastNMS.INSTANCE.field$RecipeHolder$id(holder);
                Object resourceLocation = FastNMS.INSTANCE.field$ResourceKey$location(id);
                Key recipeId = Key.of(resourceLocation.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
                ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(CoreReflections.field$SingleRecipeInput$item.get(args[0]));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(id);
                    return optionalRecipe;
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == MRecipeTypes.SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == MRecipeTypes.SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(id);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(holder));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class SetBlockStateInterceptor {
        public static final SetBlockStateInterceptor INSTANCE = new SetBlockStateInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) throws Exception {
            InjectedHolder.Section holder = (InjectedHolder.Section) thisObj;
            int x = (int) args[0];
            int y = (int) args[1];
            int z = (int) args[2];
            Object newState = args[3];
            Object previousState = superMethod.call();
            if (holder.isActive()) {
                compareAndUpdateBlockState(x, y, z, newState, previousState, holder);
            }
            return previousState;
        }
    }

    public static class GetAndSetInterceptor {
        public static final GetAndSetInterceptor INSTANCE = new GetAndSetInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedHolder.Palette holder = (InjectedHolder.Palette) thisObj;
            Object targetStates = holder.target();
            int x = (int) args[0];
            int y = (int) args[1];
            int z = (int) args[2];
            Object newState = args[3];
            Object previousState = FastNMS.INSTANCE.method$PalettedContainer$getAndSet(targetStates, x, y, z, newState);
            if (holder.isActive()) {
                compareAndUpdateBlockState(x, y, z, newState, previousState, holder);
            }
            return previousState;
        }
    }

    protected static void compareAndUpdateBlockState(int x, int y, int z, Object newState, Object previousState, InjectedHolder holder) {
        try {
            int stateId = BlockStateUtils.blockStateToId(newState);
            CESection section = holder.ceSection();
            // 如果是原版方块
            if (BlockStateUtils.isVanillaBlock(stateId)) {
                // 那么应该情况自定义块
                ImmutableBlockState previous = section.setBlockState(x, y, z, EmptyBlock.STATE);
                // 如果先前不是空气则标记
                if (!previous.isEmpty()) {
                    holder.ceChunk().setDirty(true);
                    if (Config.enableLightSystem()) {
                        updateLightIfChanged(holder, previousState, newState, newState, x, y, z);
                    }
                }
            } else {
                ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockStateUnsafe(stateId);
                ImmutableBlockState previousImmutableBlockState = section.setBlockState(x, y, z, immutableBlockState);
                if (previousImmutableBlockState == immutableBlockState) return;
                holder.ceChunk().setDirty(true);
                // 如果新方块的光照属性和客户端认为的不同
                if (Config.enableLightSystem() && !immutableBlockState.isEmpty()) {
                    updateLightIfChanged(holder, previousState, immutableBlockState.vanillaBlockState().handle(), newState, x, y, z);
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to intercept setBlockState", e);
        }
    }

    protected static void updateLightIfChanged(@This InjectedHolder thisObj, Object oldServerSideState, Object clientSideState, Object serverSideState, int x, int y, int z) {
        CEWorld world = thisObj.ceChunk().world();
        Object blockPos = LocationUtils.toBlockPos(x, y, z);
        Object serverWorld = world.world().serverWorld();
        if (clientSideState != serverSideState && FastNMS.INSTANCE.method$LightEngine$hasDifferentLightProperties(clientSideState, serverSideState, serverWorld, blockPos)) {
            SectionPos sectionPos = thisObj.cePos();
            List<SectionPos> pos = SectionPosUtils.calculateAffectedRegions((sectionPos.x() << 4) + x, (sectionPos.y() << 4) + y, (sectionPos.z() << 4) + z, 15);
            world.sectionLightUpdated(pos);
            return;
        }
        if (FastNMS.INSTANCE.method$LightEngine$hasDifferentLightProperties(oldServerSideState, serverSideState, serverWorld, blockPos)) {
            SectionPos sectionPos = thisObj.cePos();
            List<SectionPos> pos = SectionPosUtils.calculateAffectedRegions((sectionPos.x() << 4) + x, (sectionPos.y() << 4) + y, (sectionPos.z() << 4) + z, 15);
            world.sectionLightUpdated(pos);
        }
    }
}
