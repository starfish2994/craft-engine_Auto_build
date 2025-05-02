package net.momirealms.craftengine.core.util.context;

public abstract class CommonContext implements Context {
    protected final ContextHolder contexts;

    public CommonContext(ContextHolder contexts) {
        this.contexts = contexts;
    }

    @Override
    public ContextHolder contexts() {
        return contexts;
    }
}
