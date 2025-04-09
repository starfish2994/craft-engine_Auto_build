package net.momirealms.craftengine.core.font;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.context.ContextKey;

public class EmojiParameters {
    public static final ContextKey<String> KEYWORD = new ContextKey<>(Key.of("keyword"));
    public static final ContextKey<String> EMOJI = new ContextKey<>(Key.of("emoji"));
}
