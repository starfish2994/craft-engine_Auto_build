package net.momirealms.craftengine.core.item.behavior;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface ItemBehaviorFactory {

    ItemBehavior create(Key id, Map<String, Object> arguments);
}
