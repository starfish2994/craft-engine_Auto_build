package net.momirealms.craftengine.core.item.recipe.network.display;

import net.momirealms.craftengine.core.item.recipe.network.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record FurnaceRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation, int duration, float experience) implements RecipeDisplay {

    public static FurnaceRecipeDisplay read(FriendlyByteBuf buffer) {
        SlotDisplay ingredient = SlotDisplay.read(buffer);
        SlotDisplay result = SlotDisplay.read(buffer);
        SlotDisplay craftingStation = SlotDisplay.read(buffer);
        int duration = buffer.readVarInt();
        float experience = buffer.readFloat();
        return new FurnaceRecipeDisplay(ingredient, result, craftingStation, duration, experience);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.ingredient.write(buf);
        this.result.write(buf);
        this.craftingStation.write(buf);
        buf.writeVarInt(this.duration);
        buf.writeFloat(this.experience);
    }
}
