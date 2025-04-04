package net.momirealms.craftengine.core.plugin.gui;

import net.momirealms.craftengine.core.plugin.Manageable;

public interface GuiManager extends Manageable {

    Inventory createInventory(Gui gui, int size);
}
