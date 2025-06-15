package net.momirealms.craftengine.core.util;

import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;

import java.util.Map;
import java.util.function.Supplier;

public final class ResourceConfigUtils {

    private ResourceConfigUtils() {}

    public static <T> T requireNonNullOrThrow(T obj, String node) {
        if (obj == null)
            throw new LocalizedResourceConfigException(node);
        return obj;
    }

    public static <T> T requireNonNullOrThrow(T obj, Supplier<LocalizedException> exceptionSupplier) {
        if (obj == null)
            throw exceptionSupplier.get();
        return obj;
    }

    public static String requireNonEmptyStringOrThrow(Object obj, String node) {
        Object o = requireNonNullOrThrow(obj, node);
        String s = o.toString();
        if (s.isEmpty()) throw new LocalizedResourceConfigException(node);
        return s;
    }

    public static String requireNonEmptyStringOrThrow(Object obj, Supplier<LocalizedException> exceptionSupplier) {
        Object o = requireNonNullOrThrow(obj, exceptionSupplier);
        String s = o.toString();
        if (s.isEmpty()) throw exceptionSupplier.get();
        return s;
    }

    public static Object get(Map<String, Object> arguments, String... keys) {
        for (String key : keys) {
            Object value = arguments.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

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
                    throw new LocalizedResourceConfigException("warning.config.type.int", e, s, option);
                }
            }
            case Boolean b -> {
                return b ? 1 : 0;
            }
            default -> throw new LocalizedResourceConfigException("warning.config.type.int", o.toString(), option);
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
                    throw new LocalizedResourceConfigException("warning.config.type.double", e, s, option);
                }
            }
            default -> {
                throw new LocalizedResourceConfigException("warning.config.type.double", o.toString(), option);
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
                    throw new LocalizedResourceConfigException("warning.config.type.float", e, s, option);
                }
            }
            case Number number -> {
                return number.floatValue();
            }
            default -> {
                throw new LocalizedResourceConfigException("warning.config.type.float", o.toString(), option);
            }
        }
    }
}
