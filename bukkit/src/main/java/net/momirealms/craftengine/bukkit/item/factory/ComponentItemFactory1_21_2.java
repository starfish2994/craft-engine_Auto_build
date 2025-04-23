package net.momirealms.craftengine.bukkit.item.factory;

import com.saicone.rtag.data.ComponentType;
import net.momirealms.craftengine.bukkit.item.ComponentItemWrapper;
import net.momirealms.craftengine.bukkit.item.ComponentTypes;
import net.momirealms.craftengine.core.item.EquipmentData;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComponentItemFactory1_21_2 extends ComponentItemFactory1_21 {

    public ComponentItemFactory1_21_2(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    protected void tooltipStyle(ComponentItemWrapper item, String data) {
        if (data == null) {
            item.resetComponent(ComponentTypes.TOOLTIP_STYLE);
        } else {
            item.setJavaComponent(ComponentTypes.TOOLTIP_STYLE, data);
        }
    }

    @Override
    protected Optional<String> tooltipStyle(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.TOOLTIP_STYLE)) return Optional.empty();
        return Optional.ofNullable(
                (String) ComponentType.encodeJava(
                        ComponentTypes.TOOLTIP_STYLE,
                        item.getComponent(ComponentTypes.TOOLTIP_STYLE)
                ).orElse(null)
        );
    }

    @Override
    protected void itemModel(ComponentItemWrapper item, String data) {
        if (data == null) {
            item.resetComponent(ComponentTypes.ITEM_MODEL);
        } else {
            item.setJavaComponent(ComponentTypes.ITEM_MODEL, data);
        }
    }

    @Override
    protected Optional<String> itemModel(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.ITEM_MODEL)) return Optional.empty();
        return Optional.ofNullable(
                (String) ComponentType.encodeJava(
                        ComponentTypes.ITEM_MODEL,
                        item.getComponent(ComponentTypes.ITEM_MODEL)
                ).orElse(null)
        );
    }

    @Override
    protected void equippable(ComponentItemWrapper item, EquipmentData data) {
        if (data == null) {
            item.resetComponent(ComponentTypes.EQUIPPABLE);
        } else {
            item.setJavaComponent(ComponentTypes.EQUIPPABLE, data.toMap());
        }
    }

    @Override
    protected Optional<EquipmentData> equippable(ComponentItemWrapper item) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}