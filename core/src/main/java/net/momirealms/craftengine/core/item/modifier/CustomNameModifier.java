package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.AdventureHelper;

public class CustomNameModifier<I> implements ItemDataModifier<I> {
    private final String argument;

    public CustomNameModifier(String argument) {
        this.argument = Config.nonItalic() ? "<!i>" + argument : argument;
    }

    @Override
    public String name() {
        return "custom-name";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        item.customNameComponent(AdventureHelper.miniMessage().deserialize(this.argument, context.tagResolvers()));
    }

    @Override
    public void remove(Item<I> item) {
        item.customNameJson(null);
    }
}
