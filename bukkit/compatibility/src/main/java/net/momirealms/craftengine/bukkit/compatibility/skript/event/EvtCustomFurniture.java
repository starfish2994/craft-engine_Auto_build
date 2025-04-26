package net.momirealms.craftengine.bukkit.compatibility.skript.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import net.momirealms.craftengine.bukkit.api.event.FurnitureBreakEvent;
import net.momirealms.craftengine.bukkit.api.event.FurniturePlaceEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@SuppressWarnings({"unchecked"})
public class EvtCustomFurniture extends SkriptEvent {

    public static void register() {
        Skript.registerEvent("Break Furniture", EvtCustomFurniture.class, FurnitureBreakEvent.class, "(break[ing]) [[of] %-strings%]")
                .description("Called when a furniture is broken by a player.");
        Skript.registerEvent("Place Furniture", EvtCustomFurniture.class, FurniturePlaceEvent.class, "(plac(e|ing)|build[ing]) [[of] %-strings%]")
                .description("Called when a player places a furniture.");
    }

    @Nullable
    private Literal<String> ids;
    private String[] idArray;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
        if (args[0] != null) {
            ids = ((Literal<String>) args[0]);
            idArray = ids.getAll();
        }
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (ids == null)
            return true;

        String id;
        if (event instanceof FurnitureBreakEvent e) {
            id = e.furniture().id().toString();
        } else if (event instanceof FurniturePlaceEvent e) {
            id = e.furniture().id().toString();
        } else {
            return false;
        }

        return Arrays.asList(idArray).contains(id);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "break/place" + (ids != null ? " of " + ids.toString(event, debug) : "");
    }
}
