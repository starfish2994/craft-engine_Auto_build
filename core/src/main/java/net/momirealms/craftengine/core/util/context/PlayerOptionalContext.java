package net.momirealms.craftengine.core.util.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.text.minimessage.*;
import net.momirealms.craftengine.core.util.MCUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class PlayerOptionalContext extends CommonContext implements MiniMessageTextContext {
    public static final PlayerOptionalContext EMPTY = new PlayerOptionalContext(null, ContextHolder.EMPTY);
    private final Player player;
    private final LazyContextParameterProvider playerParameterProvider;
    private TagResolver[] tagResolvers;

    public PlayerOptionalContext(@Nullable Player player, @NotNull ContextHolder contexts) {
        super(contexts);
        this.player = player;
        this.playerParameterProvider = player == null ? LazyContextParameterProvider.dummy() : new PlayerParameterGetter(player);
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

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        Optional<T> optional = this.playerParameterProvider.getOptionalParameter(parameter);
        if (optional.isPresent()) {
            return optional;
        }
        return super.getOptionalParameter(parameter);
    }

    @Override
    public <T> T getParameterOrThrow(ContextKey<T> parameter) {
        return this.playerParameterProvider.getOptionalParameter(parameter).orElseGet(() -> super.getParameterOrThrow(parameter));
    }

    public static class PlayerParameterGetter implements LazyContextParameterProvider {
        private static final Map<ContextKey<?>, Function<Player, Object>> CONTEXT_FUNCTIONS = new HashMap<>();
        static {
            CONTEXT_FUNCTIONS.put(PlayerParameters.X, Entity::x);
            CONTEXT_FUNCTIONS.put(PlayerParameters.Y, Entity::y);
            CONTEXT_FUNCTIONS.put(PlayerParameters.Z, Entity::z);
            CONTEXT_FUNCTIONS.put(PlayerParameters.BLOCK_X, p -> MCUtils.fastFloor(p.x()));
            CONTEXT_FUNCTIONS.put(PlayerParameters.BLOCK_Y, p -> MCUtils.fastFloor(p.y()));
            CONTEXT_FUNCTIONS.put(PlayerParameters.BLOCK_Z, p -> MCUtils.fastFloor(p.z()));
            CONTEXT_FUNCTIONS.put(PlayerParameters.NAME, Player::name);
            CONTEXT_FUNCTIONS.put(PlayerParameters.UUID, Player::uuid);
        }

        private final Player player;

        public PlayerParameterGetter(@NotNull Player player) {
            this.player = Objects.requireNonNull(player);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
            return (Optional<T>) Optional.ofNullable(CONTEXT_FUNCTIONS.get(parameter)).map(f -> f.apply(this.player));
        }
    }
}
