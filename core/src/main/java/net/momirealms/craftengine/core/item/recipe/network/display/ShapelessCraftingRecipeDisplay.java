package net.momirealms.craftengine.core.item.recipe.network.display;

import net.momirealms.craftengine.core.item.recipe.network.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record ShapelessCraftingRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    public static ShapelessCraftingRecipeDisplay read(FriendlyByteBuf buffer) {
        List<SlotDisplay> ingredients = buffer.readCollection(ArrayList::new, SlotDisplay::read);
        SlotDisplay result = SlotDisplay.read(buffer);
        SlotDisplay craftingStation = SlotDisplay.read(buffer);
        return new ShapelessCraftingRecipeDisplay(ingredients, result, craftingStation);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.ingredients, (byteBuf, slotDisplay) -> slotDisplay.write(buf));
        this.result.write(buf);
        this.craftingStation.write(buf);
    }
}
