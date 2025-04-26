package net.momirealms.craftengine.bukkit.compatibility.skript;

import net.momirealms.craftengine.bukkit.compatibility.skript.clazz.CraftEngineClasses;
import net.momirealms.craftengine.bukkit.compatibility.skript.condition.CondIsBlockCustomBlock;
import net.momirealms.craftengine.bukkit.compatibility.skript.effect.EffPlaceCustomBlock;
import net.momirealms.craftengine.bukkit.compatibility.skript.event.EvtCustomBlock;
import net.momirealms.craftengine.bukkit.compatibility.skript.expression.ExprBlockCustomBlockID;
import net.momirealms.craftengine.bukkit.compatibility.skript.expression.ExprBlockCustomBlockState;

public class SkriptHook {

    public static void register() {
        CraftEngineClasses.register();
        EvtCustomBlock.register();
        CondIsBlockCustomBlock.register();
        ExprBlockCustomBlockID.register();
        ExprBlockCustomBlockState.register();
        EffPlaceCustomBlock.register();
    }
}
