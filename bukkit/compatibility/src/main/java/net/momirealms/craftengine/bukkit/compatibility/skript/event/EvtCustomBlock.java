package net.momirealms.craftengine.bukkit.compatibility.skript.event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockBreakEvent;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UnsafeBlockStateMatcher;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@SuppressWarnings({"unchecked"})
public class EvtCustomBlock extends SkriptEvent {

    public static void register() {
        Skript.registerEvent("Break Custom Block", EvtCustomBlock.class, CustomBlockBreakEvent.class, "(break[ing]|1¦min(e|ing)) [[of] %-unsafeblockstatematchers%]")
                .description("Called when a custom block is broken by a player. If you use 'on mine', only events where the broken block dropped something will call the trigger.");
        Skript.registerEvent("Place Custom Block", EvtCustomBlock.class, CustomBlockPlaceEvent.class, "(plac(e|ing)|build[ing]) [[of] %-unsafeblockstatematchers%]")
                .description("Called when a player places a custom block.");
    }

    @Nullable
    private Literal<UnsafeBlockStateMatcher> blocks;
    private UnsafeBlockStateMatcher[] blockArray;
    private boolean mine = false;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
        if (args[0] != null) {
            blocks = ((Literal<UnsafeBlockStateMatcher>) args[0]);
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

        ImmutableBlockState state;
        if (event instanceof CustomBlockBreakEvent customBlockBreakEvent) {
            state = customBlockBreakEvent.blockState();
        } else if (event instanceof CustomBlockPlaceEvent customBlockPlaceEvent) {
            state = customBlockPlaceEvent.blockState();
        } else {
            return false;
        }

        return Arrays.stream(blockArray).anyMatch(block -> block.matches(state));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "break/place" + (blocks != null ? " of " + blocks.toString(event, debug) : "");
    }
}
