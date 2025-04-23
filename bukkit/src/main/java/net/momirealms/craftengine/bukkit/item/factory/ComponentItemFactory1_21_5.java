package net.momirealms.craftengine.bukkit.item.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import net.momirealms.craftengine.bukkit.item.ComponentItemWrapper;
import net.momirealms.craftengine.bukkit.item.ComponentTypes;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.item.JukeboxPlayable;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComponentItemFactory1_21_5 extends ComponentItemFactory1_21_4 {

    public ComponentItemFactory1_21_5(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    protected void customName(ComponentItemWrapper item, String json) {
        if (json == null) {
            item.resetComponent(ComponentTypes.CUSTOM_NAME);
        } else {
            item.setNBTComponent(ComponentTypes.CUSTOM_NAME, ChatComponent.toTag(ComponentUtils.jsonToMinecraft(json)));
        }
    }

    @Override
    protected Optional<String> customName(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.CUSTOM_NAME)) return Optional.empty();
        return ComponentType.encodeJson(ComponentTypes.CUSTOM_NAME, item.getComponent(ComponentTypes.CUSTOM_NAME)).map(jsonElement -> GsonHelper.get().toJson(jsonElement));
    }

    @Override
    protected void itemName(ComponentItemWrapper item, String json) {
        if (json == null) {
            item.resetComponent(ComponentTypes.ITEM_NAME);
        } else {
            item.setNBTComponent(ComponentTypes.ITEM_NAME, ChatComponent.toTag(ComponentUtils.jsonToMinecraft(json)));
        }
    }

    @Override
    protected Optional<String> itemName(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.ITEM_NAME)) return Optional.empty();
        return ComponentType.encodeJson(ComponentTypes.ITEM_NAME, item.getComponent(ComponentTypes.ITEM_NAME)).map(jsonElement -> GsonHelper.get().toJson(jsonElement));
    }

    @Override
    protected Optional<List<String>> lore(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.LORE)) return Optional.empty();
        return ComponentType.encodeJson(
                ComponentTypes.LORE,
                item.getComponent(ComponentTypes.LORE)
        ).map(list -> {
           List<String> lore = new ArrayList<>();
           for (JsonElement jsonElement : (JsonArray) list) {
               lore.add(GsonHelper.get().toJson(jsonElement));
           }
           return lore;
        });
    }

    @Override
    protected void lore(ComponentItemWrapper item, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            item.resetComponent(ComponentTypes.LORE);
        } else {
            List<Object> loreTags = new ArrayList<>();
            for (String json : lore) {
                loreTags.add(ChatComponent.toTag(ComponentUtils.jsonToMinecraft(json)));
            }
            item.setNBTComponent(ComponentTypes.LORE, TagList.newTag(loreTags));
        }
    }

    @Override
    protected Optional<JukeboxPlayable> jukeboxSong(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.JUKEBOX_PLAYABLE)) return Optional.empty();
        String song = (String) ComponentType.encodeJava(
                ComponentTypes.JUKEBOX_PLAYABLE,
                item.getComponent(ComponentTypes.JUKEBOX_PLAYABLE)).orElse(null);
        if (song == null) return Optional.empty();
        return Optional.of(new JukeboxPlayable(song, true));
    }

    @Override
    protected void jukeboxSong(ComponentItemWrapper item, JukeboxPlayable data) {
        item.setJavaComponent(ComponentTypes.JUKEBOX_PLAYABLE, data.song());
    }
}
