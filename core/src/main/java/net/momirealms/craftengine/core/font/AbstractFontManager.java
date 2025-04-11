package net.momirealms.craftengine.core.font;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.util.context.PlayerContext;
import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractFontManager implements FontManager {
    private final CraftEngine plugin;
                // namespace:font font
    private final Map<Key, Font> fonts = new HashMap<>();
    // namespace:id emoji
    private final Map<Key, Emoji> emojis = new HashMap<>();
    // namespace:id image
    private final Map<Key, BitmapImage> images = new HashMap<>();
    private final Set<Integer> illegalChars = new HashSet<>();
    private final ImageParser imageParser;
    private final EmojiParser emojiParser;
    private OffsetFont offsetFont;

    protected Trie imageTagTrie;
    protected Trie emojiKeywordTrie;
    protected Map<String, Component> tagMapper;
    protected Map<String, Emoji> emojiMapper;
    // tab补全
    protected final Map<UUID, String> cachedEmojiSuggestions = new HashMap<>();
    protected final Map<UUID, String> oldCachedEmojiSuggestions = new HashMap<>();

    public AbstractFontManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.imageParser = new ImageParser();
        this.emojiParser = new EmojiParser();
    }

    @Override
    public void load() {
        this.offsetFont = Optional.ofNullable(plugin.config().settings().getSection("offset-characters"))
                .map(OffsetFont::new)
                .orElse(null);
    }

    @Override
    public void unload() {
        this.fonts.clear();
        this.images.clear();
        this.illegalChars.clear();
        this.emojis.clear();
        this.cachedEmojiSuggestions.clear();
    }

    @Override
    public Map<UUID, String> cachedEmojiSuggestions() {
        return ImmutableMap.copyOf(this.cachedEmojiSuggestions);
    }

    @Override
    public Map<UUID, String> oldCachedEmojiSuggestions() {
        return ImmutableMap.copyOf(this.oldCachedEmojiSuggestions);
    }

    @Override
    public Map<String, Component> matchTags(String json) {
        if (this.imageTagTrie == null) {
            return Collections.emptyMap();
        }
        Map<String, Component> tags = new HashMap<>();
        for (Token token : this.imageTagTrie.tokenize(json)) {
            if (token.isMatch()) {
                tags.put(token.getFragment(), this.tagMapper.get(token.getFragment()));
            }
        }
        return tags;
    }

    @Override
    public String stripTags(String text) {
        if (this.imageTagTrie == null) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        for (Token token : this.imageTagTrie.tokenize(text)) {
            if (token.isMatch()) {
                builder.append("*");
            } else {
                builder.append(token.getFragment());
            }
        }
        return builder.toString();
    }

    @Override
    public String replaceMiniMessageEmoji(String miniMessage, Player player) {
        if (this.emojiKeywordTrie == null) {
            return miniMessage;
        }
        for (Token token : this.emojiKeywordTrie.tokenize(miniMessage)) {
            if (!token.isMatch()) continue;
            Emoji emoji = this.emojiMapper.get(token.getFragment());
            if (emoji == null) continue;
            Component content = AdventureHelper.miniMessage().deserialize(
                    emoji.content(),
                    PlayerContext.of(player, ContextHolder.builder()
                            .withOptionalParameter(EmojiParameters.EMOJI, emoji.emojiImage())
                            .withParameter(EmojiParameters.KEYWORD, emoji.keywords().get(0))
                            .build()).tagResolvers()
            );
            miniMessage = miniMessage.replace(token.getFragment(), AdventureHelper.componentToMiniMessage(content));
        }
        return miniMessage;
    }

    @Override
    public String replaceJsonEmoji(String jsonText, Player player) {
        if (this.emojiKeywordTrie == null) {
            return jsonText;
        }
        Map<String, Emoji> emojis = new HashMap<>();
        for (Token token : this.emojiKeywordTrie.tokenize(jsonText)) {
            if (token.isMatch()) {
                emojis.put(token.getFragment(), this.emojiMapper.get(token.getFragment()));
            }
        }
        if (emojis.isEmpty()) return jsonText;
        Component component = AdventureHelper.jsonToComponent(jsonText);
        for (Map.Entry<String, Emoji> entry : emojis.entrySet()) {
            Emoji emoji = entry.getValue();
            if (player != null && emoji.permission() != null && !player.hasPermission(emoji.permission())) {
                continue;
            }
            component = component.replaceText(builder -> builder.matchLiteral(entry.getKey())
                    .replacement(
                            AdventureHelper.miniMessage().deserialize(
                                    emoji.content(),
                                    PlayerContext.of(player, ContextHolder.builder()
                                            .withOptionalParameter(EmojiParameters.EMOJI, emoji.emojiImage())
                                            .withParameter(EmojiParameters.KEYWORD, emoji.keywords().get(0))
                                            .build()).tagResolvers())
                    ));
        }
        return AdventureHelper.componentToJson(component);
    }

    @Override
    public ConfigSectionParser[] parsers() {
        return new ConfigSectionParser[] {this.imageParser, this.emojiParser};
    }

    @Override
    public void delayedLoad() {
        this.oldCachedEmojiSuggestions.clear();
        Optional.ofNullable(this.fonts.get(DEFAULT_FONT)).ifPresent(font -> this.illegalChars.addAll(font.codepointsInUse()));
        this.buildImageTagTrie();
        this.buildEmojiKeywordsTrie();
    }

    private void buildEmojiKeywordsTrie() {
        this.emojiMapper = new HashMap<>();
        for (Emoji emoji : this.emojis.values()) {
            for (String keyword : emoji.keywords()) {
                this.emojiMapper.put(keyword, emoji);
            }
        }
        this.emojiKeywordTrie = Trie.builder()
                .ignoreOverlaps()
                .addKeywords(this.emojiMapper.keySet())
                .build();
    }

    private void buildImageTagTrie() {
        this.tagMapper = new HashMap<>();
        for (BitmapImage image : this.images.values()) {
            String id = image.id().toString();
            this.tagMapper.put(imageTag(id), AdventureHelper.miniMessage().deserialize(image.miniMessage(0, 0)));
            this.tagMapper.put("\\" + imageTag(id), Component.text(imageTag(id)));
            for (int i = 0; i < image.rows(); i++) {
                for (int j = 0; j < image.columns(); j++) {
                    this.tagMapper.put(imageTag(id + ":" + i + ":" + j), AdventureHelper.miniMessage().deserialize(image.miniMessage(i, j)));
                    this.tagMapper.put(imageTag("\\" + id + ":" + i + ":" + j), Component.text(imageTag(id + ":" + i + ":" + j)));
                }
            }
        }
        for (int i = -256; i <= 256; i++) {
            this.tagMapper.put("<shift:" + i + ">", AdventureHelper.miniMessage().deserialize(this.offsetFont.createOffset(i, FormatUtils::miniMessageFont)));
            this.tagMapper.put("\\<shift:" + i + ">", Component.text("<shift:" + i + ">"));
        }
        this.imageTagTrie = Trie.builder()
                .ignoreOverlaps()
                .addKeywords(this.tagMapper.keySet())
                .build();
    }

    private static String imageTag(String text) {
        return "<image:" + text + ">";
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
        return Collections.unmodifiableCollection(this.fonts.values());
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
            if (emojis.containsKey(id)) {
                TranslationManager.instance().log("warning.config.emoji.duplicated", path.toString(), id.toString());
                return;
            }
            String permission = (String) section.get("permission");
            Object keywordsRaw = section.get("keywords");
            if (keywordsRaw == null) {
                TranslationManager.instance().log("warning.config.emoji.lack_keywords", path.toString(), id.toString());
                return;
            }
            List<String> keywords = MiscUtils.getAsStringList(keywordsRaw);
            if (keywords.isEmpty()) {
                TranslationManager.instance().log("warning.config.emoji.lack_keywords", path.toString(), id.toString());
                return;
            }
            String keyword = keywords.get(0);
            UUID uuid = UUID.nameUUIDFromBytes(keyword.getBytes(StandardCharsets.UTF_8));
            cachedEmojiSuggestions.put(uuid, keyword);
            String content = section.getOrDefault("content", "<arg:emoji>").toString();
            String image = null;
            if (section.containsKey("image")) {
                String rawImage = section.get("image").toString();
                String[] split = rawImage.split(":");
                if (split.length == 2) {
                    Key imageId = new Key(split[0], split[1]);
                    Optional<BitmapImage> bitmapImage = bitmapImageByImageId(imageId);
                    if (bitmapImage.isPresent()) {
                        image = bitmapImage.get().miniMessage(0, 0);
                    } else {
                        TranslationManager.instance().log("warning.config.emoji.invalid_image", path.toString(), id.toString(), rawImage);
                        return;
                    }
                } else if (split.length == 4) {
                    Key imageId = new Key(split[0], split[1]);
                    Optional<BitmapImage> bitmapImage = bitmapImageByImageId(imageId);
                    if (bitmapImage.isPresent()) {
                        image = bitmapImage.get().miniMessage(Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                    } else {
                        TranslationManager.instance().log("warning.config.emoji.invalid_image", path.toString(), id.toString(), rawImage);
                        return;
                    }
                } else {
                    TranslationManager.instance().log("warning.config.emoji.invalid_image", path.toString(), id.toString(), rawImage);
                    return;
                }
            }
            Emoji emoji = new Emoji(content, permission, image, keywords);
            emojis.put(id, emoji);
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
            if (images.containsKey(id)) {
                TranslationManager.instance().log("warning.config.image.duplicated", path.toString(), id.toString());
                return;
            }

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
