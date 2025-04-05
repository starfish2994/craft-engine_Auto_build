package net.momirealms.craftengine.bukkit.item.factory;

import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComponentItemFactory1_21_5 extends ComponentItemFactory1_21_4 {

    public ComponentItemFactory1_21_5(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    protected void customName(ItemWrapper<ItemStack> item, String json) {
        if (json == null) {
            item.removeComponent(ComponentKeys.CUSTOM_NAME);
        } else {
            item.setComponent(ComponentKeys.CUSTOM_NAME, ChatComponent.toTag(ComponentUtils.jsonToMinecraft(json)));
        }
    }

    @Override
    protected Optional<String> customName(ItemWrapper<ItemStack> item) {
        if (!item.hasComponent(ComponentKeys.CUSTOM_NAME)) return Optional.empty();
        return ComponentType.encodeJava(ComponentKeys.CUSTOM_NAME, item.getComponent(ComponentKeys.ITEM_NAME)).map(ChatComponent::fromTag).map(ComponentUtils::minecraftToJson);
    }

    @Override
    protected void itemName(ItemWrapper<ItemStack> item, String json) {
        if (json == null) {
            item.removeComponent(ComponentKeys.ITEM_NAME);
        } else {
            item.setComponent(ComponentKeys.ITEM_NAME, ChatComponent.toTag(ComponentUtils.jsonToMinecraft(json)));
        }
    }

    @Override
    protected Optional<String> itemName(ItemWrapper<ItemStack> item) {
        if (!item.hasComponent(ComponentKeys.ITEM_NAME)) return Optional.empty();
        return ComponentType.encodeJava(ComponentKeys.ITEM_NAME, item.getComponent(ComponentKeys.ITEM_NAME)).map(ChatComponent::fromTag).map(ComponentUtils::minecraftToJson);
    }

    @Override
    protected Optional<List<String>> lore(ItemWrapper<ItemStack> item) {
        if (!item.hasComponent(ComponentKeys.LORE)) return Optional.empty();
        return ComponentType.encodeJava(
                ComponentKeys.LORE,
                item.getComponent(ComponentKeys.LORE)
        ).map(list -> {
           List<String> lore = new ArrayList<>();
           List<Object> tagList = TagList.getValue(list);
           for (Object o : tagList) {
               lore.add(ComponentUtils.minecraftToJson(ChatComponent.fromTag(o)));
           }
           return lore;
        });
    }

    @Override
    protected void lore(ItemWrapper<ItemStack> item, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            item.removeComponent(ComponentKeys.LORE);
        } else {
            List<Object> loreTags = new ArrayList<>();
            for (String json : lore) {
                loreTags.add(ChatComponent.toTag(ComponentUtils.jsonToMinecraft(json)));
            }
            item.setComponent(ComponentKeys.LORE, TagList.newTag(loreTags));
        }
    }
}
