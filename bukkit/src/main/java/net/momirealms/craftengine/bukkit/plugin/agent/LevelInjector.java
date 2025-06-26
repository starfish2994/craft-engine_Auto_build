package net.momirealms.craftengine.bukkit.plugin.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitBlockInWorld;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.block.state.properties.DoubleBlockHalf;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.world.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LevelInjector {

    public static void patch() {
        Class<?> holderClass = new ByteBuddy()
                .subclass(Object.class)
                .name("net.momirealms.craftengine.bukkit.plugin.agent.LevelInjectorHolder")
                .defineField("callEventMethod", MethodHandle.class, Modifier.PUBLIC | Modifier.STATIC)
                .defineMethod("setCallEventMethod", void.class, Modifier.PUBLIC | Modifier.STATIC)
                .withParameters(MethodHandle.class)
                .intercept(new Implementation() {
                    @Override
                    public @NotNull InstrumentedType prepare(@NotNull InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }

                    @Override
                    public @NotNull ByteCodeAppender appender(@NotNull Target implementationTarget) {
                        return (methodVisitor, implementationContext, instrumentedMethod) -> {
                            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                            methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC,
                                    "net/momirealms/craftengine/bukkit/plugin/agent/LevelInjectorHolder",
                                    "callEventMethod",
                                    "Ljava/lang/invoke/MethodHandle;");
                            methodVisitor.visitInsn(Opcodes.RETURN);
                            return new ByteCodeAppender.Size(1, 1);
                        };
                    }
                })
                .make()
                .load(Bukkit.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        try {
            Method setCallEventMethod = holderClass.getMethod("setCallEventMethod", MethodHandle.class);
            Method callEvent = LevelInjector.class.getMethod("callEvent", Object.class, Object[].class);
            MethodHandle callEventMethod = MethodHandles.lookup().unreflect(callEvent)
                    .asType(MethodType.methodType(void.class, Object.class, Object[].class));
            setCallEventMethod.invoke(null, callEventMethod);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(ElementMatchers.is(CoreReflections.clazz$Level))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(CallEventAdvice.class)
                                .on(ElementMatchers.is(CoreReflections.method$Level$destroyBlock))))
                .installOn(ByteBuddyAgent.install());
    }

    public static class CallEventAdvice {

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This Object level, @Advice.AllArguments Object[] args) {
            try {
                Class<?> holder = Class.forName("net.momirealms.craftengine.bukkit.plugin.agent.LevelInjectorHolder");
                MethodHandle destroyBlockEventMethod = (MethodHandle) holder.getField("callEventMethod").get(null);
                destroyBlockEventMethod.invokeExact(level, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void callEvent(Object level, Object[] args) {
        Object pos = args[0];
        boolean dropBlock = (boolean) args[1];
        if (!dropBlock) return;
        Object state = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, pos);
        int stateId = BlockStateUtils.blockStateToId(state);
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (blockState == null || blockState.isEmpty()) return;
        BlockPos blockPos = LocationUtils.fromBlockPos(pos);
        for (Property<?> property : blockState.getProperties()) {
            if (property.valueClass() == DoubleBlockHalf.class) return; // 退退退不处理了气死我了
        }
        org.bukkit.World bukkitWorld = FastNMS.INSTANCE.method$Level$getCraftWorld(level);
        World world = BukkitWorldManager.instance().wrap(bukkitWorld);
        WorldPosition position = new WorldPosition(world, Vec3d.atCenterOf(blockPos));
        Block block = bukkitWorld.getBlockAt(blockPos.x(), blockPos.y(), blockPos.z());
        ContextHolder.Builder builder = ContextHolder.builder()
                .withParameter(DirectContextParameters.POSITION, position)
                .withParameter(DirectContextParameters.BLOCK, new BukkitBlockInWorld(block));
        for (Item<Object> item : blockState.getDrops(builder, world, null)) {
            world.dropItemNaturally(position, item);
        }
        world.playBlockSound(position, blockState.sounds().breakSound());
        FastNMS.INSTANCE.method$Level$levelEvent(level, WorldEvents.BLOCK_BREAK_EFFECT, pos, stateId);
    }

}
