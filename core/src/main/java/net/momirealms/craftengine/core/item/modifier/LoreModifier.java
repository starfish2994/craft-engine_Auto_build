package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.AdventureHelper;

import java.util.List;

public class LoreModifier<I> implements ItemDataModifier<I> {
    private final List<String> argument;

    public LoreModifier(List<String> argument) {
        this.argument = Config.nonItalic() ? argument.stream().map(it -> "<!i>" + it).toList() : argument;
    }

    @Override
    public String name() {
        return "lore";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        item.lore(this.argument.stream().map(it -> AdventureHelper.componentToJson(AdventureHelper.miniMessage().deserialize(
                it, context.tagResolvers()))).toList());
    }

    @Override
    public void remove(Item<I> item) {
        item.lore(null);
    }
}
