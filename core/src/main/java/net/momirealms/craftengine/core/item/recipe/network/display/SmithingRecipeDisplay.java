package net.momirealms.craftengine.core.item.recipe.network.display;

import net.momirealms.craftengine.core.item.recipe.network.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record SmithingRecipeDisplay(SlotDisplay template, SlotDisplay base, SlotDisplay addition, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    public static SmithingRecipeDisplay read(FriendlyByteBuf buffer) {
        SlotDisplay template = SlotDisplay.read(buffer);
        SlotDisplay base = SlotDisplay.read(buffer);
        SlotDisplay addition = SlotDisplay.read(buffer);
        SlotDisplay result = SlotDisplay.read(buffer);
        SlotDisplay craftingStation = SlotDisplay.read(buffer);
        return new SmithingRecipeDisplay(template, base, addition, result, craftingStation);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.template.write(buf);
        this.base.write(buf);
        this.addition.write(buf);
        this.result.write(buf);
        this.craftingStation.write(buf);
    }
}
