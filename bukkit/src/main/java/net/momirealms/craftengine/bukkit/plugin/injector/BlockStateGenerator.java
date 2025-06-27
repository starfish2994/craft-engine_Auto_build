package net.momirealms.craftengine.bukkit.plugin.injector;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MItems;
import net.momirealms.craftengine.core.block.CustomBlockStateHolder;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public final class BlockStateGenerator {
    private static MethodHandle constructor$CraftEngineBlockState;
    public static Object instance$StateDefinition$Factory;

    public static void init() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
        String packageWithName = BlockStateGenerator.class.getName();
        String generatedStateClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineBlockState";
        DynamicType.Builder<?> stateBuilder = byteBuddy
                .subclass(CoreReflections.clazz$BlockState, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedStateClassName)
                .defineField("immutableBlockState", ImmutableBlockState.class, Visibility.PUBLIC)
                .implement(CustomBlockStateHolder.class)
                .method(ElementMatchers.named("customBlockState"))
                .intercept(FieldAccessor.ofField("immutableBlockState"))
                .method(ElementMatchers.named("setCustomBlockState"))
                .intercept(FieldAccessor.ofField("immutableBlockState"))
                .method(ElementMatchers.is(CoreReflections.method$BlockStateBase$getDrops))
                .intercept(MethodDelegation.to(GetDropsInterceptor.INSTANCE));
        Class<?> clazz$CraftEngineBlock = stateBuilder.make().load(BlockStateGenerator.class.getClassLoader()).getLoaded();
        constructor$CraftEngineBlockState = MethodHandles.publicLookup().in(clazz$CraftEngineBlock)
                .findConstructor(clazz$CraftEngineBlock, MethodType.methodType(void.class, CoreReflections.clazz$Block, Reference2ObjectArrayMap.class, MapCodec.class))
                .asType(MethodType.methodType(CoreReflections.clazz$BlockState, CoreReflections.clazz$Block, Reference2ObjectArrayMap.class, MapCodec.class));

        String generatedFactoryClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineStateFactory";
        DynamicType.Builder<?> factoryBuilder = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedFactoryClassName)
                .implement(CoreReflections.clazz$StateDefinition$Factory)
                .method(ElementMatchers.named("create"))
                .intercept(MethodDelegation.to(CreateStateInterceptor.INSTANCE));

        Class<?> clazz$Factory = factoryBuilder.make().load(BlockStateGenerator.class.getClassLoader()).getLoaded();
        instance$StateDefinition$Factory = ReflectionUtils.getTheOnlyConstructor(clazz$Factory).newInstance();
    }

    public static class GetDropsInterceptor {
        public static final GetDropsInterceptor INSTANCE = new GetDropsInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            ImmutableBlockState state = ((CustomBlockStateHolder) thisObj).customBlockState();
            if (state == null) return List.of();
            Object builder = args[0];
            return List.of(FastNMS.INSTANCE.constructor$ItemStack(MItems.WATER_BUCKET, 1));
        }
    }

    public static class CreateStateInterceptor {
        public static final CreateStateInterceptor INSTANCE = new CreateStateInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) throws Throwable {
            return constructor$CraftEngineBlockState.invoke(args[0], args[1], args[2]);
        }
    }
}
