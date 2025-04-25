package net.momirealms.craftengine.bukkit.compatibility.skript.classes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import net.momirealms.craftengine.core.block.BlockStateParser;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;

public class CraftEngineClasses {

    public static void register() {
        Classes.registerClass(new ClassInfo<>(ImmutableBlockState.class, "customblockstate")
                .user("custom block state")
                .name("Custom Block State")
                .serializer(new Serializer<>() {
                    @Override
                    public Fields serialize(ImmutableBlockState o) {
                        Fields f = new Fields();
                        f.putObject("customblockstate", o.toString());
                        return f;
                    }

                    @Override
                    public void deserialize(ImmutableBlockState o, Fields f) {
                    }

                    @Override
                    public ImmutableBlockState deserialize(Fields f) throws StreamCorruptedException {
                        String data = f.getObject("customblockstate", String.class);
                        assert data != null;
                        try {
                            return BlockStateParser.deserialize(data);
                        } catch (IllegalArgumentException ex) {
                            throw new StreamCorruptedException("Invalid block data: " + data);
                        }
                    }

                    @Override
                    public boolean mustSyncDeserialization() {
                        return true;
                    }

                    @Override
                    protected boolean canBeInstantiated() {
                        return false;
                    }
                })
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
