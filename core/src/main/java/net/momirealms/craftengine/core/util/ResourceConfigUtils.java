package net.momirealms.craftengine.core.util;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;

import java.util.Map;

public final class ResourceConfigUtils {

    private ResourceConfigUtils() {}

    public static int getAsInt(Object o, String option) {
        switch (o) {
            case null -> {
                return 0;
            }
            case Integer i -> {
                return i;
            }
            case Number number -> {
                return number.intValue();
            }
            case String s -> {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    throw new LocalizedResourceConfigException("warning.config.cast_int", e, s, option);
                }
            }
            case Boolean b -> {
                return b ? 1 : 0;
            }
            default -> throw new LocalizedResourceConfigException("warning.config.cast_int", o.toString(), option);
        }
    }

    public static double getAsDouble(Object o, String option) {
        switch (o) {
            case null -> {
                return 0.0;
            }
            case Double d -> {
                return d;
            }
            case Number n -> {
                return n.doubleValue();
            }
            case String s -> {
                try {
                    return Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    throw new LocalizedResourceConfigException("warning.config.cast_double", e, s, option);
                }
            }
            default -> {
                throw new LocalizedResourceConfigException("warning.config.cast_double", o.toString(), option);
            }
        }
    }

    public static float getAsFloat(Object o, String option) {
        switch (o) {
            case null -> {
                return 0.0f;
            }
            case Float f -> {
                return f;
            }
            case String s -> {
                try {
                    return Float.parseFloat(s);
                } catch (NumberFormatException e) {
                    throw new LocalizedResourceConfigException("warning.config.cast_float", e, s, option);
                }
            }
            case Number number -> {
                return number.floatValue();
            }
            default -> {
                throw new LocalizedResourceConfigException("warning.config.cast_float", o.toString(), option);
            }
        }
    }
}
