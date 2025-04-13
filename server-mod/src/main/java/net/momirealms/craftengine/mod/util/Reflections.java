package net.momirealms.craftengine.mod.util;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class Reflections {

    public static final Method method$DefaultedRegistry$get = requireNonNull(
            ReflectionUtils.getMethod(
                    DefaultedRegistry.class, Object.class, ResourceLocation.class
            )
    );

    public static final Field field$BlockBehaviour$Properties$id = ReflectionUtils.getDeclaredField(
            BlockBehaviour.Properties.class, ResourceKey.class, 0
    );
}
