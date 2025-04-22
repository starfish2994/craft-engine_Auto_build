package net.momirealms.craftengine.core.plugin.event;

import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public class Triggers {
    public static final Map<Key, Trigger> TRIGGERS = new HashMap<>();

    public static final Trigger USE_ITEM = create(Key.of("use_item"));
    public static final Trigger INTERACT = create(Key.of("interact"));
    public static final Trigger CONSUME = create(Key.of("consume"));
    public static final Trigger BREAK = create(Key.of("break"));

    private static Trigger create(Key id) {
        Trigger trigger = new Trigger(id);
        TRIGGERS.put(id, trigger);
        return trigger;
    }
}
