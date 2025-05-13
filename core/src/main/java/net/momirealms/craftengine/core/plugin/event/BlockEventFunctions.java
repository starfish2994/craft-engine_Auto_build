package net.momirealms.craftengine.core.plugin.event;

import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.function.CommandFunction;
import net.momirealms.craftengine.core.plugin.context.function.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.plugin.context.function.FunctionFactory;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public class BlockEventFunctions {

    static {
        register(CommonFunctions.COMMAND, new CommandFunction.FactoryImpl<>(BlockEventConditions::fromMap));
    }

    public static void register(Key key, FunctionFactory<PlayerOptionalContext> factory) {
        Holder.Reference<FunctionFactory<PlayerOptionalContext>> holder = ((WritableRegistry<FunctionFactory<PlayerOptionalContext>>) BuiltInRegistries.PLAYER_BLOCK_FUNCTION_FACTORY)
                .registerForHolder(new ResourceKey<>(Registries.PLAYER_BLOCK_FUNCTION_FACTORY.location(), key));
        holder.bindValue(factory);
    }

    public static Function<PlayerOptionalContext> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "TODO I18N");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        FunctionFactory<PlayerOptionalContext> factory = BuiltInRegistries.PLAYER_BLOCK_FUNCTION_FACTORY.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("TODO I18N", type);
        }
        return factory.create(map);
    }
}
