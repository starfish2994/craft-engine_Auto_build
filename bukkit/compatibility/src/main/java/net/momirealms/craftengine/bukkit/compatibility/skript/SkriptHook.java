package net.momirealms.craftengine.bukkit.compatibility.skript;

import net.momirealms.craftengine.bukkit.compatibility.skript.clazz.CraftEngineClasses;
import net.momirealms.craftengine.bukkit.compatibility.skript.condition.CondIsCustomBlock;
import net.momirealms.craftengine.bukkit.compatibility.skript.condition.CondIsFurniture;
import net.momirealms.craftengine.bukkit.compatibility.skript.effect.EffPlaceCustomBlock;
import net.momirealms.craftengine.bukkit.compatibility.skript.effect.EffPlaceFurniture;
import net.momirealms.craftengine.bukkit.compatibility.skript.effect.EffRemoveFurniture;
import net.momirealms.craftengine.bukkit.compatibility.skript.event.EvtCustomBlock;
import net.momirealms.craftengine.bukkit.compatibility.skript.expression.ExprBlockCustomBlockID;
import net.momirealms.craftengine.bukkit.compatibility.skript.expression.ExprBlockCustomBlockState;
import net.momirealms.craftengine.bukkit.compatibility.skript.expression.ExprEntityFurnitureID;

public class SkriptHook {

    public static void register() {
        CraftEngineClasses.register();
        EvtCustomBlock.register();
        CondIsCustomBlock.register();
        CondIsFurniture.register();
        ExprBlockCustomBlockID.register();
        ExprBlockCustomBlockState.register();
        ExprEntityFurnitureID.register();
        EffPlaceCustomBlock.register();
        EffPlaceFurniture.register();
        EffRemoveFurniture.register();
    }
}
