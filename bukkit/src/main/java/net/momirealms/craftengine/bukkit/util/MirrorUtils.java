package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.util.Mirror;

public class MirrorUtils {

    private MirrorUtils() {}

    public static Mirror fromNMSMirror(Object mirror) {
        try {
            int index = (int) Reflections.method$Mirror$ordinal.invoke(mirror);
            return Mirror.values()[index];
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object toNMSMirror(Mirror mirror) {
        switch (mirror) {
            case FRONT_BACK -> {
                return Reflections.instance$Mirror$FRONT_BACK;
            }
            case LEFT_RIGHT -> {
                return Reflections.instance$Mirror$LEFT_RIGHT;
            }
            default -> {
                return Reflections.instance$Mirror$NONE;
            }
        }
    }
}
