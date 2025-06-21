package net.momirealms.craftengine.core.pack.model;

public class ModernItemModel {
    private final ItemModel itemModel;
    private final boolean oversizedInGui;
    private final boolean handAnimationOnSwap;

    public ModernItemModel(ItemModel itemModel, boolean handAnimationOnSwap, boolean oversizedInGui) {
        this.handAnimationOnSwap = handAnimationOnSwap;
        this.itemModel = itemModel;
        this.oversizedInGui = oversizedInGui;
    }

    public boolean handAnimationOnSwap() {
        return handAnimationOnSwap;
    }

    public ItemModel itemModel() {
        return itemModel;
    }

    public boolean oversizedInGui() {
        return oversizedInGui;
    }
}
