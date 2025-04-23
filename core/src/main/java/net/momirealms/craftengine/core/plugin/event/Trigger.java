package net.momirealms.craftengine.core.plugin.event;

import net.momirealms.craftengine.core.util.Key;

public class Trigger {
    private final Key id;

    public Trigger(Key id) {
        this.id = id;
    }

    public Key id() {
        return id;
    }
}
