package net.momirealms.craftengine.bukkit.compatibility.skript.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.StringUtils;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockBreakEvent;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@SuppressWarnings({"unchecked"})
public class EvtCustomBlock extends SkriptEvent {

    public static void register() {
        Skript.registerEvent("Break Custom Block", EvtCustomBlock.class, CustomBlockBreakEvent.class, "[customblock] (break[ing]|1¦min(e|ing)) [[of] %-strings%]")
                .description("Called when a custom block is broken by a player. If you use 'on mine', only events where the broken block dropped something will call the trigger.");
        Skript.registerEvent("Place Custom Block", EvtCustomBlock.class, CustomBlockPlaceEvent.class, "[customblock] (plac(e|ing)|build[ing]) [[of] %-strings%]")
                .description("Called when a player places a custom block.");
    }

    @Nullable
    private Literal<String> blocks;
    private String[] blockArray;
    private boolean mine = false;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
        if (args[0] != null) {
            blocks = ((Literal<String>) args[0]);
            blockArray = blocks.getAll();
        }
        mine = parser.mark == 1;
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (mine && event instanceof CustomBlockBreakEvent customBlockBreakEvent) {
            if (!BlockStateUtils.isCorrectTool(customBlockBreakEvent.blockState(), customBlockBreakEvent.player().getItemInHand(InteractionHand.MAIN_HAND))) {
                return false;
            }
        }
        if (blocks == null)
            return true;

        String blockType;
        String blockState;
        if (event instanceof CustomBlockBreakEvent customBlockBreakEvent) {
            blockType = customBlockBreakEvent.customBlock().id().toString();
            blockState = customBlockBreakEvent.blockState().toString();
        } else if (event instanceof CustomBlockPlaceEvent customBlockPlaceEvent) {
            blockType = customBlockPlaceEvent.customBlock().id().toString();
            blockState = customBlockPlaceEvent.blockState().toString();
        } else {
            return false;
        }

        return Arrays.stream(blockArray).anyMatch(block -> StringUtils.equals(blockType, block, true) || StringUtils.equals(blockState, block, true));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "break/place" + (blocks != null ? " of " + blocks.toString(event, debug) : "");
    }
}
