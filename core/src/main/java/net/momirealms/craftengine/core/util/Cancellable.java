package net.momirealms.craftengine.core.util;

public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean cancel);

    static Cancellable dummy() {
        return new Dummy();
    }

    class Dummy implements Cancellable {
        private boolean cancelled;

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }
    }
}
