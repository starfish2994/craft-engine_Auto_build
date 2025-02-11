package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.minimessage.ImageTag;
import net.momirealms.craftengine.core.plugin.minimessage.PlaceholderTag;
import net.momirealms.craftengine.core.util.AdventureHelper;

public class ItemNameModifier<I> implements ItemModifier<I> {
    private final String parameter;

    public ItemNameModifier(String parameter) {
        this.parameter = parameter;
    }
    @Override
    public String name() {
        return "item-name";
    }

    @Override
    public void apply(Item<I> item, Player player) {
        item.itemName(AdventureHelper.componentToJson(AdventureHelper.miniMessage().deserialize(
                parameter, ImageTag.instance(), new PlaceholderTag(player))));
    }
}
