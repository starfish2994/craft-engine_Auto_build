package net.momirealms.craftengine.core.font;

import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractFontManager implements FontManager {
    private final CraftEngine plugin;
                // namespace:font font
    private final Map<Key, Font> fonts = new HashMap<>();
                // namespace:id image
    private final Map<Key, BitmapImage> images = new HashMap<>();
    private final Set<Integer> illegalChars = new HashSet<>();
    private final ImageParser imageParser;
    private final EmojiParser emojiParser;

    private OffsetFont offsetFont;

    public AbstractFontManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.imageParser = new ImageParser();
        this.emojiParser = new EmojiParser();
    }

    @Override
    public void load() {
        this.offsetFont = Optional.ofNullable(plugin.configManager().settings().getSection("offset-characters"))
                .map(OffsetFont::new)
                .orElse(null);
    }

    @Override
    public void unload() {
        this.fonts.clear();
        this.images.clear();
        this.illegalChars.clear();
    }

    @Override
    public ConfigSectionParser[] parsers() {
        return new ConfigSectionParser[] {this.imageParser, this.emojiParser};
    }

    @Override
    public void delayedLoad() {
        Optional.ofNullable(this.fonts.get(DEFAULT_FONT)).ifPresent(font -> this.illegalChars.addAll(font.codepointsInUse()));
    }

    @Override
    public boolean isDefaultFontInUse() {
        return !this.illegalChars.isEmpty();
    }

    @Override
    public boolean isIllegalCodepoint(int codepoint) {
        return this.illegalChars.contains(codepoint);
    }

    @Override
    public Collection<Font> fonts() {
        return new ArrayList<>(this.fonts.values());
    }

    @Override
    public Optional<BitmapImage> bitmapImageByCodepoint(Key font, int codepoint) {
        return fontById(font).map(f -> f.bitmapImageByCodepoint(codepoint));
    }

    @Override
    public Optional<BitmapImage> bitmapImageByImageId(Key id) {
        return Optional.ofNullable(this.images.get(id));
    }

    @Override
    public int codepointByImageId(Key key, int x, int y) {
        BitmapImage image = this.images.get(key);
        if (image == null) return -1;
        return image.codepointAt(x, y);
    }

    @Override
    public String createOffsets(int offset, FontTagFormatter tagFormatter) {
        return Optional.ofNullable(this.offsetFont).map(it -> it.createOffset(offset, tagFormatter)).orElse("");
    }

    @Override
    public Optional<Font> fontById(Key id) {
        return Optional.ofNullable(this.fonts.get(id));
    }

    private Font getOrCreateFont(Key key) {
        return this.fonts.computeIfAbsent(key, Font::new);
    }

    public class EmojiParser implements ConfigSectionParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"emoji", "emojis"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.EMOJI;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {

        }
    }

    public class ImageParser implements ConfigSectionParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"images", "image"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.IMAGE;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            Object heightObj = section.get("height");
            if (heightObj == null) {
                TranslationManager.instance().log("warning.config.image.lack_height", path.toString(), id.toString());
                return;
            }

            int height = MiscUtils.getAsInt(heightObj);
            int ascent = MiscUtils.getAsInt(section.getOrDefault("ascent", height - 1));
            if (height < ascent) {
                TranslationManager.instance().log("warning.config.image.height_smaller_than_ascent", path.toString(), id.toString());
                return;
            }

            Object file = section.get("file");
            if (file == null) {
                TranslationManager.instance().log("warning.config.image.no_file", path.toString(), id.toString());
                return;
            }

            String resourceLocation = file.toString().replace("\\", "/");
            if (!ResourceLocation.isValid(resourceLocation)) {
                TranslationManager.instance().log("warning.config.image.invalid_resource_location", path.toString(), id.toString(), resourceLocation);
                return;
            }

            String fontName = (String) section.getOrDefault("font", "minecraft:default");
            if (!ResourceLocation.isValid(fontName)) {
                TranslationManager.instance().log("warning.config.image.invalid_font_name", path.toString(), id.toString(), fontName);
                return;
            }

            Key fontKey = Key.withDefaultNamespace(fontName, id.namespace());
            Font font = getOrCreateFont(fontKey);
            List<char[]> chars;
            if (section.containsKey("chars")) {
                chars = MiscUtils.getAsStringList(section.get("chars")).stream().map(it -> {
                    if (it.startsWith("\\u")) {
                        return CharacterUtils.decodeUnicodeToChars(it);
                    } else {
                        return it.toCharArray();
                    }
                }).toList();
            } else {
                String character = (String) section.get("char");
                if (character == null) {
                    TranslationManager.instance().log("warning.config.image.lack_char", path.toString(), id.toString());
                    return;
                }
                if (character.length() == 1) {
                    chars = List.of(character.toCharArray());
                } else {
                    chars = List.of(CharacterUtils.decodeUnicodeToChars(character));
                }
            }

            int size = -1;
            int[][] codepointGrid = new int[chars.size()][];
            for (int i = 0; i < chars.size(); ++i) {
                int[] codepoints = CharacterUtils.charsToCodePoints(chars.get(i));
                for (int codepoint : codepoints) {
                    if (font.isCodepointInUse(codepoint)) {
                        BitmapImage image = font.bitmapImageByCodepoint(codepoint);
                        TranslationManager.instance().log("warning.config.image.codepoint_in_use",
                                path.toString(),
                                id.toString(),
                                fontKey.toString(),
                                CharacterUtils.encodeCharsToUnicode(Character.toChars(codepoint)),
                                new String(Character.toChars(codepoint)),
                                image.id().toString()
                        );
                        return;
                    }
                }
                codepointGrid[i] = codepoints;
                if (size == -1) size = codepoints.length;
                if (size != codepoints.length) {
                    TranslationManager.instance().log("warning.config.image.invalid_codepoint_grid", path.toString(), id.toString());
                    return;
                }
            }

            if (!resourceLocation.endsWith(".png")) resourceLocation += ".png";
            Key namespacedPath = Key.of(resourceLocation);
            Path targetImagePath = pack.resourcePackFolder()
                    .resolve("assets")
                    .resolve(namespacedPath.namespace())
                    .resolve("textures")
                    .resolve(namespacedPath.value());

            if (!Files.exists(targetImagePath)) {
                TranslationManager.instance().log("warning.config.image.file_not_exist", path.toString(), id.toString(), targetImagePath.toString());
                // DO NOT RETURN, JUST GIVE WARNINGS
            }

            BitmapImage bitmapImage = new BitmapImage(id, fontKey, height, ascent, resourceLocation, codepointGrid);
            for (int[] y : codepointGrid) {
                for (int x : y) {
                    font.addBitMapImage(x, bitmapImage);
                }
            }

            AbstractFontManager.this.images.put(id, bitmapImage);
        }
    }
}
