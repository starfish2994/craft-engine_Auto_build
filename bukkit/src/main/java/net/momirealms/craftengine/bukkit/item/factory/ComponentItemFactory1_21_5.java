package net.momirealms.craftengine.bukkit.item.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
        return ComponentType.encodeJson(ComponentKeys.CUSTOM_NAME, item.getComponent(ComponentKeys.CUSTOM_NAME)).map(jsonElement -> GsonHelper.get().toJson(jsonElement));
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
        return ComponentType.encodeJson(ComponentKeys.ITEM_NAME, item.getComponent(ComponentKeys.ITEM_NAME)).map(jsonElement -> GsonHelper.get().toJson(jsonElement));
    }

    @Override
    protected Optional<List<String>> lore(ItemWrapper<ItemStack> item) {
        if (!item.hasComponent(ComponentKeys.LORE)) return Optional.empty();
        return ComponentType.encodeJson(
                ComponentKeys.LORE,
                item.getComponent(ComponentKeys.LORE)
        ).map(list -> {
           List<String> lore = new ArrayList<>();
           for (JsonElement jsonElement : (JsonArray) list) {
               lore.add(GsonHelper.get().toJson(jsonElement));
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
