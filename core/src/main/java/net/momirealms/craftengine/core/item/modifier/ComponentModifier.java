package net.momirealms.craftengine.core.item.modifier;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.YamlUtils;

import java.util.Map;

public class ComponentModifier<I> implements ItemModifier<I> {
    private final Map<String, Object> parameters;

    public ComponentModifier(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public ComponentModifier(Section section) {
        this.parameters = YamlUtils.sectionToMap(section);
    }

    @Override
    public String name() {
        return "components";
    }

    @Override
    public void apply(Item<I> item, Player player) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            item.setComponent(key, value);
        }
    }
}
