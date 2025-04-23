package net.momirealms.craftengine.bukkit.item;

import com.saicone.rtag.RtagItem;
import net.momirealms.craftengine.core.item.ItemWrapper;
import org.bukkit.inventory.ItemStack;

public class LegacyItemWrapper implements ItemWrapper<ItemStack> {
    private final RtagItem rtagItem;
    private int count;

    public LegacyItemWrapper(RtagItem rtagItem, int count) {
        this.rtagItem = rtagItem;
        this.count = count;
    }

    @Override
    public ItemStack getItem() {
        ItemStack itemStack = this.rtagItem.getItem();
        itemStack.setAmount(this.count);
        return itemStack;
    }

    public boolean set(Object value, Object... path) {
        return this.rtagItem.set(value, path);
    }

    public boolean add(Object value, Object... path) {
        return this.rtagItem.add(value, path);
    }

    public <V> V get(Object... path) {
        return this.rtagItem.get(path);
    }

    public int count() {
        return this.count;
    }

    public void count(int amount) {
        if (amount < 0) amount = 0;
        this.count = amount;
    }

    public <V> V getExact(Object... path) {
        return this.rtagItem.get(path);
    }

    public boolean remove(Object... path) {
        return this.rtagItem.remove(path);
    }

    public boolean hasTag(Object... path) {
        return this.rtagItem.hasTag(path);
    }

    public void update() {
        this.rtagItem.update();
    }

    @Override
    public ItemStack load() {
        ItemStack itemStack = this.rtagItem.load();
        itemStack.setAmount(this.count);
        return itemStack;
    }

    @Override
    public Object getLiteralObject() {
        return this.rtagItem.getLiteralObject();
    }

    @Override
    public ItemWrapper<ItemStack> copyWithCount(int count) {
        return new LegacyItemWrapper(new RtagItem(this.rtagItem.loadCopy()), count);
    }
}