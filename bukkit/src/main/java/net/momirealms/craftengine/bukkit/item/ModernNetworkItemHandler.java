package net.momirealms.craftengine.bukkit.item;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public class ModernNetworkItemHandler implements NetworkItemHandler<ItemStack> {
    private final BukkitItemManager itemManager;

    public ModernNetworkItemHandler(BukkitItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public Optional<Item<ItemStack>> c2s(Item<ItemStack> wrapped, ItemBuildContext context) {
        Tag customData = wrapped.getNBTComponent(ComponentTypes.CUSTOM_DATA);
        if (!(customData instanceof CompoundTag compoundTag)) return Optional.empty();
        CompoundTag networkData = compoundTag.getCompound(NETWORK_ITEM_TAG);
        if (networkData == null) return Optional.empty();
        compoundTag.remove(NETWORK_ITEM_TAG);
        for (Map.Entry<String, Tag> entry : networkData.entrySet()) {
            if (entry.getValue() instanceof CompoundTag tag) {
                NetworkItemHandler.apply(entry.getKey(), tag, wrapped);
            }
        }
        // todo 可能会是 !custom_data吗，不可能，绝对不可能！
        if (compoundTag.isEmpty()) wrapped.resetComponent(ComponentTypes.CUSTOM_DATA);
        else wrapped.setNBTComponent(ComponentTypes.CUSTOM_DATA, compoundTag);
        return Optional.of(wrapped);
    }

    @Override
    public Optional<Item<ItemStack>> s2c(Item<ItemStack> wrapped, ItemBuildContext context) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = wrapped.getCustomItem();
        if (optionalCustomItem.isEmpty()) {
            if (!Config.interceptItem()) return Optional.empty();
            return new OtherItem(wrapped).process();
        } else {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            if (!customItem.hasClientBoundDataModifier()) {
                if (!Config.interceptItem()) return Optional.empty();
                return new OtherItem(wrapped).process();
            } else {
                CompoundTag customData = Optional.ofNullable(wrapped.getNBTComponent(ComponentTypes.CUSTOM_DATA)).map(CompoundTag.class::cast).orElse(new CompoundTag());
                CompoundTag tag = new CompoundTag();
                for (ItemDataModifier<ItemStack> modifier : customItem.clientBoundDataModifiers()) {
                    modifier.prepareNetworkItem(wrapped, context, tag);
                    modifier.apply(wrapped, context);
                }
                if (Config.interceptItem()) {
                    if (!tag.containsKey(ComponentIds.ITEM_NAME)) {
                        Tag nameTag = wrapped.getNBTComponent(ComponentTypes.ITEM_NAME);
                        if (nameTag != null) {
                            String tagStr = nameTag.getAsString();
                            Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(tagStr);
                            if (!tokens.isEmpty()) {
                                wrapped.setNBTComponent(ComponentKeys.ITEM_NAME, AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(nameTag), tokens)));
                                tag.put(ComponentIds.ITEM_NAME, NetworkItemHandler.pack(Operation.ADD, nameTag));
                            }
                        }
                    }
                    if (!tag.containsKey(ComponentIds.CUSTOM_NAME)) {
                        Tag nameTag = wrapped.getNBTComponent(ComponentTypes.CUSTOM_NAME);
                        if (nameTag != null) {
                            String tagStr = nameTag.getAsString();
                            Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(tagStr);
                            if (!tokens.isEmpty()) {
                                wrapped.setNBTComponent(ComponentKeys.CUSTOM_NAME, AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(nameTag), tokens)));
                                tag.put(ComponentIds.CUSTOM_NAME, NetworkItemHandler.pack(Operation.ADD, nameTag));
                            }
                        }
                    }
                    if (!tag.containsKey(ComponentIds.LORE)) {
                        Tag loreTag = wrapped.getNBTComponent(ComponentTypes.LORE);
                        if (loreTag instanceof ListTag listTag) {
                            ListTag newLore = new ListTag();
                            boolean changed = false;
                            String tagStr = listTag.getAsString();
                            Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(tagStr);
                            if (tokens.isEmpty()) {
                                newLore.add(tag);
                            } else {
                                newLore.add(AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(tag), tokens)));
                                changed = true;
                            }
                            if (changed) {
                                wrapped.setNBTComponent(ComponentKeys.LORE, newLore);
                                tag.put(ComponentIds.LORE, NetworkItemHandler.pack(Operation.ADD, listTag));
                            }
                        }
                    }
                }
                if (tag.isEmpty()) return Optional.empty();
                customData.put(NETWORK_ITEM_TAG, tag);
                wrapped.setNBTComponent(ComponentTypes.CUSTOM_DATA, customData);
                return Optional.of(wrapped);
            }
        }
    }

    static class OtherItem {
        private final Item<ItemStack> item;
        private boolean globalChanged = false;
        private CompoundTag tag;

        public OtherItem(Item<ItemStack> item) {
            this.item = item;
        }

        public Optional<Item<ItemStack>> process() {
            processModernLore();
            processModernCustomName();
            processModernItemName();
            if (this.globalChanged) {
                CompoundTag customData = Optional.ofNullable(this.item.getNBTComponent(ComponentTypes.CUSTOM_DATA)).map(CompoundTag.class::cast).orElse(new CompoundTag());
                customData.put(NETWORK_ITEM_TAG, getOrCreateTag());
                this.item.setNBTComponent(ComponentKeys.CUSTOM_DATA, customData);
                return Optional.of(this.item);
            } else {
                return Optional.empty();
            }
        }

        private void processModernItemName() {
            Tag nameTag = this.item.getNBTComponent(ComponentTypes.ITEM_NAME);
            if (nameTag == null) return;
            String tagStr = nameTag.getAsString();
            Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(tagStr);
            if (!tokens.isEmpty()) {
                this.item.setNBTComponent(ComponentKeys.ITEM_NAME, AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(nameTag), tokens)));
                this.globalChanged = true;
                getOrCreateTag().put(ComponentKeys.ITEM_NAME.asString(), NetworkItemHandler.pack(Operation.ADD, nameTag));
            }
        }

        private void processModernCustomName() {
            Tag nameTag = this.item.getNBTComponent(ComponentTypes.CUSTOM_NAME);
            if (nameTag == null) return;
            String tagStr = nameTag.getAsString();
            Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(tagStr);
            if (!tokens.isEmpty()) {
                this.item.setNBTComponent(ComponentKeys.CUSTOM_NAME, AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(nameTag), tokens)));
                this.globalChanged = true;
                getOrCreateTag().put(ComponentKeys.CUSTOM_NAME.asString(), NetworkItemHandler.pack(Operation.ADD, nameTag));
            }
        }

        private void processModernLore() {
            Tag loreTag = this.item.getNBTComponent(ComponentTypes.LORE);
            boolean changed = false;
            if (!(loreTag instanceof ListTag listTag)) {
                return;
            }
            ListTag newLore = new ListTag();
            for (Tag tag : listTag) {
                String tagStr = tag.getAsString();
                Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(tagStr);
                if (tokens.isEmpty()) {
                    newLore.add(tag);
                } else {
                    newLore.add(AdventureHelper.componentToNbt(AdventureHelper.replaceText(AdventureHelper.nbtToComponent(tag), tokens)));
                    changed = true;
                }
            }
            if (changed) {
                this.globalChanged = true;
                this.item.setNBTComponent(ComponentKeys.LORE, newLore);
                getOrCreateTag().put(ComponentKeys.LORE.asString(), NetworkItemHandler.pack(Operation.ADD, listTag));
            }
        }

        private CompoundTag getOrCreateTag() {
            if (this.tag == null) {
                this.tag = new CompoundTag();
            }
            return this.tag;
        }
    }
}
