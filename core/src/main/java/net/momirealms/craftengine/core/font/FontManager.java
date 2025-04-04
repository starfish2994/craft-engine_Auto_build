package net.momirealms.craftengine.core.font;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.FormatUtils;
import net.momirealms.craftengine.core.util.Key;

import java.util.Collection;
import java.util.Optional;

public interface FontManager extends Manageable {
    Key DEFAULT_FONT = Key.of("minecraft:default");
    String BYPASS_BOOK = "craftengine.filter.bypass.book";
    String BYPASS_SIGN = "craftengine.filter.bypass.sign";
    String BYPASS_CHAT = "craftengine.filter.bypass.chat";
    String BYPASS_COMMAND = "craftengine.filter.bypass.command";
    String BYPASS_ANVIL = "craftengine.filter.bypass.anvil";

    ConfigSectionParser[] parsers();

    boolean isDefaultFontInUse();

    boolean isIllegalCodepoint(int codepoint);

    Collection<Font> fonts();

    Optional<BitmapImage> bitmapImageByCodepoint(Key font, int codepoint);

    default Optional<BitmapImage> bitmapImageByChars(Key font, char[] chars) {
        return bitmapImageByCodepoint(font, CharacterUtils.charsToCodePoint(chars));
    }

    Optional<BitmapImage> bitmapImageByImageId(Key imageId);

    Optional<Font> fontById(Key font);

    int codepointByImageId(Key imageId, int x, int y);

    default int codepointByImageId(Key imageId) {
        return this.codepointByImageId(imageId, 0, 0);
    }

    default char[] charsByImageId(Key imageId) {
        return charsByImageId(imageId, 0, 0);
    }

    default char[] charsByImageId(Key imageId, int x, int y) {
        return Character.toChars(this.codepointByImageId(imageId, x, y));
    }

    String createOffsets(int offset, FontTagFormatter tagFormatter);

    default String createMiniMessageOffsets(int offset) {
        return createOffsets(offset, FormatUtils::miniMessageFont);
    }

    default String createMineDownOffsets(int offset) {
        return createOffsets(offset, FormatUtils::mineDownFont);
    }

    default String createRawOffsets(int offset) {
        return createOffsets(offset, (raw, font) -> raw);
    }
}
