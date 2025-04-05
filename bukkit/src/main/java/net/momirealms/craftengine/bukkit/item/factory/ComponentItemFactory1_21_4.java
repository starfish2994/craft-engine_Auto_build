package net.momirealms.craftengine.bukkit.item.factory;

import com.saicone.rtag.data.ComponentType;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComponentItemFactory1_21_4 extends ComponentItemFactory {

    public ComponentItemFactory1_21_4(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    protected Optional<Integer> customModelData(ItemWrapper<ItemStack> item) {
        if (!item.hasComponent(ComponentKeys.CUSTOM_MODEL_DATA)) return Optional.empty();
        Optional<Object> optional = ComponentType.encodeJava(ComponentKeys.CUSTOM_MODEL_DATA, item.getComponent(ComponentKeys.CUSTOM_MODEL_DATA));
        if (optional.isEmpty()) return Optional.empty();
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) optional.get();
        @SuppressWarnings("unchecked")
        List<Float> floats = (List<Float>) data.get("floats");
        if (floats == null || floats.isEmpty()) return Optional.empty();
        return Optional.of((int) Math.floor(floats.get(0)));
    }

    @Override
    protected void customModelData(ItemWrapper<ItemStack> item, Integer data) {
        if (data == null) {
            item.removeComponent(ComponentKeys.CUSTOM_MODEL_DATA);
        } else {
            item.setComponent(ComponentKeys.CUSTOM_MODEL_DATA, Map.of("floats", List.of(data.floatValue())));
        }
    }
}
