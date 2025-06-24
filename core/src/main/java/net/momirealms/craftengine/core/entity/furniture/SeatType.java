package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.ArrayList;
import java.util.List;

public class SeatType {
    public static final Key SIT = Key.of("craftengine:sit");
    public static final Key LAY = Key.of("craftengine:lay");
    public static final Key CRAWL = Key.of("craftengine:crawl");

    public static void register(Key key, SeatFactory factory) {
        Holder.Reference<SeatFactory> holder = ((WritableRegistry<SeatFactory>) BuiltInRegistries.SEAT_FACTORY)
                .registerForHolder(new ResourceKey<>(Registries.SEAT_FACTORY.location(), key));
        holder.bindValue(factory);
    }

    public static Seat fromString(String s) {
        int lastSpaceIndex = s.lastIndexOf(' ');

        Key type = SIT;
        SeatFactory factory;
        String numericPart;

        if (lastSpaceIndex != -1) {
            numericPart = s.substring(lastSpaceIndex + 1);
            try {
                Float.parseFloat(numericPart);
            } catch (NumberFormatException e) {
                type = Key.withDefaultNamespace(numericPart, "craftengine");
                s = s.substring(0, lastSpaceIndex);
                lastSpaceIndex = s.lastIndexOf(' ');
            }
        }

        List<String> split = new ArrayList<>();
        int start = 0;
        while (lastSpaceIndex != -1) {
            split.add(s.substring(start, lastSpaceIndex));
            start = lastSpaceIndex + 1;
            lastSpaceIndex = s.indexOf(' ', start);
        }
        if (start < s.length()) {
            split.add(s.substring(start));
        }

        factory = BuiltInRegistries.SEAT_FACTORY.getValue(type);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.furniture.seat.invalid_type", type.toString());
        }
        return factory.create(split);
    }
}
