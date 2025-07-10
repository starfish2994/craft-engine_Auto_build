package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Obsolete
public interface LegacyRecipe {

    void write(FriendlyByteBuf buf);

    static LegacyRecipe read(FriendlyByteBuf buf) {
        if (VersionHelper.isOrAbove1_20_5()) {
            Key id = buf.readKey();
            Key type = buf.readKey();
            return BuiltInRegistries.LEGACY_RECIPE_TYPE.getValue(type).read(id, buf);
        } else {
            Key type = buf.readKey();
            Key id = buf.readKey();
            return BuiltInRegistries.LEGACY_RECIPE_TYPE.getValue(type).read(id, buf);
        }
    }

    record Type(Function<FriendlyByteBuf, LegacyRecipe> reader) {

        public LegacyRecipe read(Key id, FriendlyByteBuf buf) {
            return this.reader.apply(buf);
        }
    }
}
