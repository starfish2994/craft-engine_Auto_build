package net.momirealms.craftengine.core.font;

import net.momirealms.craftengine.core.util.Key;

import javax.annotation.Nullable;

public class Emoji {
    private final Key font;
    private final String image;
    private final String permission;

    public Emoji(Key font, String image, String permission) {
        this.font = font;
        this.image = image;
        this.permission = permission;
    }

    public Key font() {
        return font;
    }

    public String image() {
        return image;
    }

    @Nullable
    public String permission() {
        return permission;
    }
}
