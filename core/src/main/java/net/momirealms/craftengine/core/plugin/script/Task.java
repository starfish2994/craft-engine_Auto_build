package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Task {

    Map<Key, Block> blocks();

    Key id();

    @Nullable
    Block byId(Key id);

    @Nullable
    Block byAction(Action<?> action);

    static Task create(Key key, Map<Key, Block> blocks) {
        return new TaskImpl(key, blocks);
    }

    class TaskImpl implements Task {
        private final Key id;
        private final Map<Key, Block> blocks = new LinkedHashMap<>();

        public TaskImpl(Key id, Map<Key, Block> blocks) {
            this.blocks.putAll(blocks);
            this.id = id;
        }

        @Nullable
        @Override
        public Block byId(Key id) {
            return this.blocks.get(id);
        }

        @Override
        public Map<Key, Block> blocks() {
            return Collections.unmodifiableMap(this.blocks);
        }

        @Override
        public Key id() {
            return this.id;
        }

        @Override
        public @Nullable Block byAction(Action<?> action) {
            for (Block block : blocks.values()) {
                if (block.contains(action)) {
                    return block;
                }
            }
            return null;
        }
    }
}
