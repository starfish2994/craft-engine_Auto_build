package net.momirealms.craftengine.core.util.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.text.minimessage.*;
import net.momirealms.craftengine.core.util.context.parameter.CommonParameterProvider;
import net.momirealms.craftengine.core.util.context.parameter.PlayerParameterProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerOptionalContext extends AbstractAdditionalCommonContext implements MiniMessageTagContext {
    public static final PlayerOptionalContext EMPTY = new PlayerOptionalContext(null, ContextHolder.EMPTY);
    private final Player player;

    public PlayerOptionalContext(@Nullable Player player, @NotNull ContextHolder contexts) {
        super(contexts, player == null ? List.of(new CommonParameterProvider()) : List.of(new CommonParameterProvider(), new PlayerParameterProvider(player)));
        this.player = player;
    }

    public PlayerOptionalContext(@Nullable Player player, @NotNull ContextHolder contexts, @NotNull List<LazyContextParameterProvider> providers) {
        super(contexts, providers);
        this.player = player;
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player, @NotNull ContextHolder contexts) {
        return new PlayerOptionalContext(player, contexts);
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
