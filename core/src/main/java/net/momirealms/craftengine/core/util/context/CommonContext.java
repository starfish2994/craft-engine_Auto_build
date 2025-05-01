package net.momirealms.craftengine.core.util.context;

public abstract class CommonContext implements Context {
    protected final ContextHolder holder;

    public CommonContext(ContextHolder holder) {
        this.holder = holder;
    }

    @Override
    public ContextHolder contexts() {
        return holder;
    }
}
