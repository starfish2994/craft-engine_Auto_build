package net.momirealms.craftengine.bukkit.compatibility.skript.classes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.momirealms.craftengine.core.block.BlockStateParser;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import org.jetbrains.annotations.Nullable;

public class CraftEngineClasses {

    public static void register() {
        Classes.registerClass(new ClassInfo<>(ImmutableBlockState.class, "customblockstate")
                .user("custom block state")
                .name("Custom Block State")
                .parser(new Parser<>() {
                    @Override
                    public String toString(ImmutableBlockState o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(ImmutableBlockState o) {
                        return "customblockstate:" + o.toString();
                    }

                    @Override
                    public @Nullable ImmutableBlockState parse(String s, ParseContext context) {
                        return BlockStateParser.deserialize(s);
                    }
                })
        );
    }
}
