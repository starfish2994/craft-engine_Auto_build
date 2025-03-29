package net.momirealms.craftengine.core.plugin.locale;

import net.momirealms.craftengine.core.block.BlockStateParser;
import net.momirealms.craftengine.core.block.BlockStateVariantProvider;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class I18NData {
    public final Map<String, String> translations = new HashMap<>();

    public void addTranslations(Map<String, String> data) {
        addBlockName(data);
        translations.putAll(data);
    }

    public void addTranslation(String key, String value) {
        this.translations.put(key, value);
    }

    @Nullable
    public String translate(String key) {
        return this.translations.get(key);
    }

    @Override
    public String toString() {
        return "I18NData{" + translations + "}";
    }

    public static void merge(Map<String, I18NData> target, Map<String, I18NData> source) {
        source.forEach((key, value) -> {
            I18NData copy = new I18NData();
            copy.addTranslations(value.translations);
            target.merge(key, copy, (existing, newData) -> {
                existing.addTranslations(newData.translations);
                return existing;
            });
        });
    }

    private static void addBlockName(Map<String, String> data) {
        String[] keyBuffer = new String[data.size()];
        int validKeyCount = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.length() > 11 && key.startsWith("block_name:")) {
                keyBuffer[validKeyCount++] = key;
            }
        }

        for (String key : keyBuffer) {
            String value = data.remove(key);
            ImmutableBlockState states = BlockStateParser.deserialize(key.substring(11));
            if (states == null) continue;

            BlockStateVariantProvider variantProvider = states.owner().value().variantProvider();
            Collection<ImmutableBlockState> stateCollection = variantProvider.states();
            for (ImmutableBlockState state : stateCollection) {
                Object blockState = state.customBlockState().handle();
                String id = blockState.toString();
                int first = -1, last = -1;
                for (int i = 0; i < id.length(); i++) {
                    char c = id.charAt(i);
                    if (c == '{' && first == -1) {
                        first = i;
                    } else if (c == '}') {
                        last = i;
                    }
                }
                if (first == -1 || last == -1 || last <= first) {
                    CraftEngine.instance().logger().warn("Invalid block ID format: " + id);
                    continue;
                }
                int length = last - first - 1;
                char[] chars = new char[length];
                id.getChars(first + 1, last, chars, 0);
                for (int i = 0; i < length; i++) {
                    if (chars[i] == ':') {
                        chars[i] = '.';
                    }
                }
                String blockId = new String(chars);
                data.put("block." + blockId, value);
            }
        }
    }
}
