package net.momirealms.craftengine.bukkit.item;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import net.momirealms.sparrow.nbt.StringTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModernNetworkItemHandler implements NetworkItemHandler {
    private final BukkitItemManager itemManager;

    public ModernNetworkItemHandler(BukkitItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public Optional<ItemStack> c2s(ItemStack itemStack, ItemBuildContext context) {
        Item<ItemStack> wrapped = this.itemManager.wrap(itemStack);
        if (wrapped == null) return Optional.empty();
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
        if (compoundTag.isEmpty()) {
            wrapped.resetComponent(ComponentTypes.CUSTOM_DATA);
        } else {
            wrapped.setNBTComponent(ComponentTypes.CUSTOM_DATA, compoundTag);
        }
        return Optional.of(wrapped.load());
    }

    @Override
    public Optional<ItemStack> s2c(ItemStack itemStack, ItemBuildContext context) {
        Item<ItemStack> wrapped = this.itemManager.wrap(itemStack);
        if (wrapped == null) return Optional.empty();
        Optional<CustomItem<ItemStack>> optionalCustomItem = wrapped.getCustomItem();
        if (optionalCustomItem.isEmpty()) {
            if (!Config.interceptItem()) return Optional.empty();
            return new OtherItem(wrapped).process();
        } else {
            return Optional.empty();
        }
    }

    static class OtherItem {
        private final Item<ItemStack> item;
        private boolean globalChanged = false;
        private CompoundTag tag;

        public OtherItem(Item<ItemStack> item) {
            this.item = item;
        }

        public Optional<ItemStack> process() {
            if (VersionHelper.isOrAbove1_21_5()) {
                processModernLore();
                processModernCustomName();
                processModernItemName();
            } else {
                processLore();
                processCustomName();
                processItemName();
            }
            if (this.globalChanged) {
                CompoundTag customData = Optional.ofNullable(this.item.getNBTComponent(ComponentTypes.CUSTOM_DATA)).map(CompoundTag.class::cast).orElse(new CompoundTag());
                customData.put(NETWORK_ITEM_TAG, getOrCreateTag());
                this.item.setNBTComponent(ComponentKeys.CUSTOM_DATA, customData);
                return Optional.of(this.item.load());
            } else {
                return Optional.empty();
            }
        }

        private void processLore() {
            Optional<List<String>> optionalLore = this.item.loreJson();
            if (optionalLore.isPresent()) {
                boolean changed = false;
                List<String> lore = optionalLore.get();
                List<String> newLore = new ArrayList<>(lore.size());
                for (String line : lore) {
                    Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(line);
                    if (tokens.isEmpty()) {
                        newLore.add(line);
                    } else {
                        newLore.add(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens)));
                        changed = true;
                    }
                }
                if (changed) {
                    this.globalChanged = true;
                    this.item.loreJson(newLore);
                    ListTag listTag = new ListTag();
                    for (String line : lore) {
                        listTag.add(new StringTag(line));
                    }
                    getOrCreateTag().put(ComponentKeys.LORE.asString(), NetworkItemHandler.pack(Operation.ADD, listTag));
                }
            }
        }

        private void processItemName() {
            Optional<String> optionalItemName = this.item.itemNameJson();
            if (optionalItemName.isPresent()) {
                String line = optionalItemName.get();
                Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(line);
                if (!tokens.isEmpty()) {
                    this.item.itemNameJson(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens)));
                    this.globalChanged = true;
                    getOrCreateTag().put(ComponentKeys.ITEM_NAME.asString(), NetworkItemHandler.pack(Operation.ADD, new StringTag(line)));
                }
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

        private void processCustomName() {
            Optional<String> optionalCustomName = this.item.customNameJson();
            if (optionalCustomName.isPresent()) {
                String line = optionalCustomName.get();
                Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(line);
                if (!tokens.isEmpty()) {
                    this.item.customNameJson(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens)));
                    this.globalChanged = true;
                    getOrCreateTag().put(ComponentKeys.CUSTOM_NAME.asString(), NetworkItemHandler.pack(Operation.ADD, new StringTag(line)));
                }
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
            if (loreTag == null) return;
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
