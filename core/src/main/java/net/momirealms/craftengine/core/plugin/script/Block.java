package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.Key;

public interface Block {

    int size();

    Action<?>[] actions();

    Key id();

    Action<?> byIndex(int index);

    int indexOf(Action<?> action);

    boolean contains(Action<?> action);

    static Block create(Key id, Action<?>... actions) {
        return new BlockImpl(id, actions);
    }

    class BlockImpl implements Block {
        private final Key id;
        private final Action<?>[] actions;

        public BlockImpl(Key id, Action<?>[] actions) {
            this.actions = actions;
            this.id = id;
        }

        @Override
        public int size() {
            return actions.length;
        }

        @Override
        public Action<?>[] actions() {
            return this.actions;
        }

        @Override
        public Key id() {
            return this.id;
        }

        @Override
        public boolean contains(Action<?> action) {
            for (Action<?> value : this.actions) {
                if (value.equals(action)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Action<?> byIndex(int index) {
            return this.actions[index];
        }

        @Override
        public int indexOf(final Action<?> action) {
            for (int i = 0; i < this.actions.length; i++) {
                if (this.actions[i].equals(action)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
