package net.momirealms.craftengine.core.util;

import java.lang.reflect.Field;

import static java.util.Objects.requireNonNull;

public class VersionHelper {
    private static final Class<?> clazz$DetectedVersion = requireNonNull(
            ReflectionUtils.getClazz("net.minecraft.DetectedVersion", "net.minecraft.MinecraftVersion"));
    private static final Class<?> clazz$WorldVersion = requireNonNull(
            ReflectionUtils.getClazz("net.minecraft.WorldVersion"));
    public static final Field field$DetectedVersion$BUILT_IN = requireNonNull(
            ReflectionUtils.getDeclaredField(clazz$DetectedVersion, clazz$WorldVersion, 0));
    public static final Field field$DetectedVersion$name = requireNonNull(
            ReflectionUtils.getDeclaredField(clazz$DetectedVersion, String.class, 1));
    static {
        try {
            Object detectedVersion = field$DetectedVersion$BUILT_IN.get(null);
            String name = (String) field$DetectedVersion$name.get(detectedVersion);
            init(name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static float version;
    private static boolean mojmap;
    private static boolean folia;
    private static boolean paper;

    private static boolean v1_20;
    private static boolean v1_20_1;
    private static boolean v1_20_2;
    private static boolean v1_20_3;
    private static boolean v1_20_4;
    private static boolean v1_20_5;
    private static boolean v1_20_6;
    private static boolean v1_21;
    private static boolean v1_21_1;
    private static boolean v1_21_2;
    private static boolean v1_21_3;
    private static boolean v1_21_4;
    private static boolean v1_21_5;

    public static void init(String serverVersion) {
        String[] split = serverVersion.split("\\.");
        version = Float.parseFloat(split[1] + "." + (split.length == 3 ? split[2] : "0"));
        checkMojMap();
        checkFolia();
        checkPaper();
        v1_20 = version >= 20f;
        v1_20_1 = version >= 20.1f;
        v1_20_2 = version >= 20.2f;
        v1_20_3 = version >= 20.3f;
        v1_20_4 = version >= 20.4f;
        v1_20_5 = version >= 20.5f;
        v1_20_6 = version >= 20.6f;
        v1_21 = version >= 21f;
        v1_21_1 = version >= 21.1f;
        v1_21_2 = version >= 21.2f;
        v1_21_3 = version >= 21.3f;
        v1_21_4 = version >= 21.4f;
        v1_21_5 = version >= 21.5f;
    }

    public static float version() {
        return version;
    }

    private static void checkMojMap() {
        // Check if the server is Mojmap
        try {
            Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket");
            mojmap = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static void checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static void checkPaper() {
        try {
            Class.forName("io.papermc.paper.adventure.PaperAdventure");
            paper = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static boolean isFolia() {
        return folia;
    }

    public static boolean isPaper() {
        return paper;
    }

    public static boolean isMojmap() {
        return mojmap;
    }

    public static boolean isOrAbove1_20() {
        return v1_20;
    }

    public static boolean isOrAbove1_20_1() {
        return v1_20_1;
    }

    public static boolean isOrAbove1_20_2() {
        return v1_20_2;
    }

    public static boolean isOrAbove1_20_3() {
        return v1_20_3;
    }

    public static boolean isOrAbove1_20_4() {
        return v1_20_4;
    }

    public static boolean isOrAbove1_20_5() {
        return v1_20_5;
    }

    public static boolean isOrAbove1_20_6() {
        return v1_20_6;
    }

    public static boolean isOrAbove1_21() {
        return v1_21;
    }

    public static boolean isOrAbove1_21_1() {
        return v1_21_1;
    }

    public static boolean isOrAbove1_21_2() {
        return v1_21_2;
    }

    public static boolean isOrAbove1_21_3() {
        return v1_21_3;
    }

    public static boolean isOrAbove1_21_4() {
        return v1_21_4;
    }

    public static boolean isOrAbove1_21_5() {
        return v1_21_5;
    }
}