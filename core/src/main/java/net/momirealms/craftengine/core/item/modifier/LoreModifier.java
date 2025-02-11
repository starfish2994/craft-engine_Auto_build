package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.minimessage.ImageTag;
import net.momirealms.craftengine.core.plugin.minimessage.PlaceholderTag;
import net.momirealms.craftengine.core.util.AdventureHelper;

import java.util.List;

public class LoreModifier<I> implements ItemModifier<I> {
    private final List<String> parameter;

    public LoreModifier(List<String> parameter) {
        this.parameter = parameter;
    }

    @Override
    public String name() {
        return "lore";
    }

    @Override
    public void apply(Item<I> item, Player player) {
        item.lore(parameter.stream().map(it -> AdventureHelper.componentToJson(AdventureHelper.miniMessage().deserialize(
                it, ImageTag.instance(), new PlaceholderTag(player)))).toList());
    }
}
