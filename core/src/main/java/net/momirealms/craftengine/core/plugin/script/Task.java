package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Task {

    Map<Key, TaskBlock> blocks();

    Key id();

    @Nullable
    TaskBlock byId(Key id);

    @Nullable
    TaskBlock byAction(Action<?> action);

    static Task create(Key key, Map<Key, TaskBlock> blocks) {
        return new TaskImpl(key, blocks);
    }

    class TaskImpl implements Task {
        private final Key id;
        private final Map<Key, TaskBlock> blocks = new LinkedHashMap<>();

        public TaskImpl(Key id, Map<Key, TaskBlock> blocks) {
            this.blocks.putAll(blocks);
            this.id = id;
        }

        @Nullable
        @Override
        public TaskBlock byId(Key id) {
            return this.blocks.get(id);
        }

        @Override
        public Map<Key, TaskBlock> blocks() {
            return Collections.unmodifiableMap(this.blocks);
        }

        @Override
        public Key id() {
            return this.id;
        }

        @Override
        public @Nullable TaskBlock byAction(Action<?> action) {
            for (TaskBlock block : blocks.values()) {
                if (block.contains(action)) {
                    return block;
                }
            }
            return null;
        }
    }
}
