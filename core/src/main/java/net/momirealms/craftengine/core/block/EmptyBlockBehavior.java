package net.momirealms.craftengine.core.block;

public class EmptyBlockBehavior extends BlockBehavior {
    public static final EmptyBlockBehavior INSTANCE = new EmptyBlockBehavior();

    @Override
    public CustomBlock block() {
        return EmptyBlock.INSTANCE;
    }
}
