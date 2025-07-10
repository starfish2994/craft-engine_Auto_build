package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CraftingRecipeCategory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@ApiStatus.Obsolete
public class LegacyShapedRecipe implements LegacyRecipe {
    private final int width;
    private final int height;
    private final List<LegacyIngredient> ingredients;
    private final Item<?> result;
    private final Key id;
    private final String group;
    private final CraftingRecipeCategory category;
    private final boolean showNotification;

    public LegacyShapedRecipe(int width, int height, List<LegacyIngredient> ingredients, Item<?> result, Key id, String group, CraftingRecipeCategory category, boolean showNotification) {
        this.category = category;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.id = id;
        this.group = group;
        this.showNotification = showNotification;
    }

    private static final BiFunction<Key, FriendlyByteBuf, LegacyShapedRecipe> READER = VersionHelper.isOrAbove1_20_3() ?
            (id, buf) -> {
                String group = buf.readUtf();
                int category = buf.readVarInt();
                int width = buf.readVarInt();
                int height = buf.readVarInt();
                int size = width * height;
                List<LegacyIngredient> ingredients = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ingredients.set(i, LegacyIngredient.read(buf));
                }
                Item<?> result = CraftEngine.instance().itemManager().decode(buf);
                boolean flag = buf.readBoolean();
                return new LegacyShapedRecipe(width, height, ingredients, result, id, group, CraftingRecipeCategory.byId(category), flag);
            } :
            (id, buf) -> {
                int width = buf.readVarInt();
                int height = buf.readVarInt();
                String group = buf.readUtf();
                int category = buf.readVarInt();
                int size = width * height;
                List<LegacyIngredient> ingredients = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ingredients.set(i, LegacyIngredient.read(buf));
                }
                Item<?> result = CraftEngine.instance().itemManager().decode(buf);
                boolean flag = buf.readBoolean();
                return new LegacyShapedRecipe(width, height, ingredients, result, id, group, CraftingRecipeCategory.byId(category), flag);
            };

    public static LegacyShapedRecipe read(Key id, FriendlyByteBuf buf) {
        return READER.apply(id, buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }
}
