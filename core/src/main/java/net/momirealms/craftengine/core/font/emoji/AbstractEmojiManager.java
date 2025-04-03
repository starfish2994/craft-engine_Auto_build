package net.momirealms.craftengine.core.font.emoji;

import net.momirealms.craftengine.core.plugin.CraftEngine;

public abstract class AbstractEmojiManager implements EmojiManager {
    protected CraftEngine plugin;

    public AbstractEmojiManager(CraftEngine plugin) {
        this.plugin = plugin;
    }


}
