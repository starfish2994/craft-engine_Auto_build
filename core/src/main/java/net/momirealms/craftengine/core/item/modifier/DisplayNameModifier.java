package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.minimessage.ImageTag;
import net.momirealms.craftengine.core.plugin.minimessage.PlaceholderTag;
import net.momirealms.craftengine.core.util.AdventureHelper;

public class DisplayNameModifier<I> implements ItemModifier<I> {
    private final String parameter;

    public DisplayNameModifier(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String name() {
        return "display-name";
    }

    @Override
    public void apply(Item<I> item, Player player) {
        item.displayName(AdventureHelper.componentToJson(AdventureHelper.miniMessage().deserialize(
                    parameter, ImageTag.instance(), new PlaceholderTag(player))));
    }
}
