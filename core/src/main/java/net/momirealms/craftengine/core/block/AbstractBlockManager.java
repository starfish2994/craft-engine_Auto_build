package net.momirealms.craftengine.core.block;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.core.pack.model.generation.AbstractModelGenerator;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public abstract class AbstractBlockManager extends AbstractModelGenerator implements BlockManager {

    public AbstractBlockManager(CraftEngine plugin) {
        super(plugin);
    }
}
