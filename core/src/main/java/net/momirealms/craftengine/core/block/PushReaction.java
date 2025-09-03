package net.momirealms.craftengine.core.block;

public enum PushReaction {
    NORMAL(false),
    DESTROY(true),
    BLOCK(true),
    IGNORE(false),
    PUSH_ONLY(false);

    private final boolean allowedForBlockEntity;

    PushReaction(boolean allowedForBlockEntity) {
        this.allowedForBlockEntity = allowedForBlockEntity;
    }

    public boolean allowedForBlockEntity() {
        return allowedForBlockEntity;
    }
}
