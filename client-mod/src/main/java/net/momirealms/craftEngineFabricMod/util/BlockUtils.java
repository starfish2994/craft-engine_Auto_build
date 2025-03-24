package net.momirealms.craftEngineFabricMod.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import static java.util.Objects.requireNonNull;

public class BlockUtils {
    private final static Field COLLIDABLE_FIELD = requireNonNull(getDeclaredField(AbstractBlock.Settings.class, boolean.class, 0));

    @Nullable
    public static Field getDeclaredField(final Class<?> clazz, final Class<?> type, int index) {
        int i = 0;
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                if (index == i) {
                    return setAccessible(field);
                }
                i++;
            }
        }
        return null;
    }

    @NotNull
    public static <T extends AccessibleObject> T setAccessible(@NotNull final T o) {
        o.setAccessible(true);
        return o;
    }

    public static boolean canPassThrough(BlockState state) {
        try {
            if (state == null) return false;
            AbstractBlock.Settings settings = state.getBlock().getSettings();
            boolean collidable = COLLIDABLE_FIELD.getBoolean(settings);
            return !collidable;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access 'collidable' field", e);
        }
    }
}
