package net.momirealms.craftengine.bukkit.item;

import com.saicone.rtag.RtagItem;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistryOps;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

public class LegacyItemWrapper implements ItemWrapper<ItemStack> {
    private final RtagItem rtagItem;

    public LegacyItemWrapper(RtagItem rtagItem) {
        this.rtagItem = rtagItem;
    }

    public boolean setTag(Object value, Object... path) {
        if (value instanceof Tag tag) {
            return this.rtagItem.set(MRegistryOps.SPARROW_NBT.convertTo(MRegistryOps.NBT, tag), path);
        } else {
            return this.rtagItem.set(value, path);
        }
    }

    public boolean add(Object value, Object... path) {
        if (value instanceof Tag tag) {
            return this.rtagItem.add(MRegistryOps.SPARROW_NBT.convertTo(MRegistryOps.NBT, tag), path);
        } else {
            return this.rtagItem.add(value, path);
        }
    }

    public <V> V getJavaTag(Object... path) {
        return this.rtagItem.get(path);
    }

    public Tag getNBTTag(Object... path) {
        Object tag = getExactTag(path);
        if (tag == null) return null;
        return MRegistryOps.NBT.convertTo(MRegistryOps.SPARROW_NBT, tag);
    }

    public int count() {
        return getItem().getAmount();
    }

    public void count(int amount) {
        if (amount < 0) amount = 0;
        getItem().setAmount(amount);
    }

    public Object getExactTag(Object... path) {
        return this.rtagItem.getExact(path);
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
        return this.rtagItem.load();
    }

    @Override
    public ItemStack getItem() {
        return this.rtagItem.getItem();
    }

    @Override
    public Object getLiteralObject() {
        return this.rtagItem.getLiteralObject();
    }

    @Override
    public ItemWrapper<ItemStack> copyWithCount(int count) {
        ItemStack copied = this.rtagItem.loadCopy();
        copied.setAmount(count);
        return new LegacyItemWrapper(new RtagItem(copied));
    }

    @Override
    public void shrink(int amount) {
        this.count(count() - amount);
    }
}