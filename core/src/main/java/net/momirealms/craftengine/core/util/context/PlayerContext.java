package net.momirealms.craftengine.core.util.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.text.minimessage.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerContext extends CommonContext implements MiniMessageTextContext {
    public static final PlayerContext EMPTY = new PlayerContext(null, ContextHolder.EMPTY);
    private final Player player;
    private TagResolver[] tagResolvers;

    public PlayerContext(@Nullable Player player, @NotNull ContextHolder contexts) {
        super(contexts);
        this.player = player;
    }

    @NotNull
    public static PlayerContext of(@Nullable Player player, @NotNull ContextHolder contexts) {
        return new PlayerContext(player, contexts);
    }

    @Nullable
    public Player player() {
        return this.player;
    }

    @Override
    @NotNull
    public TagResolver[] tagResolvers() {
        if (this.tagResolvers == null) {
            this.tagResolvers = new TagResolver[]{ShiftTag.INSTANCE, ImageTag.INSTANCE, new PlaceholderTag(this.player), new I18NTag(this), new NamedArgumentTag(this)};
        }
        return this.tagResolvers;
    }
}
