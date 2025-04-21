package net.momirealms.craftengine.bukkit.advancement;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.advancement.AbstractAdvancementManager;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BukkitAdvancementManager extends AbstractAdvancementManager {
    private final BukkitCraftEngine plugin;
    private final AdvancementParser advancementParser;
    private final Map<Key, JsonElement> advancements = new HashMap<>();

    public BukkitAdvancementManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        this.advancementParser = new AdvancementParser();
    }

    public void unload() {
        advancements.clear();
    }

    @Override
    public ConfigSectionParser parser() {
        return this.advancementParser;
    }

    public class AdvancementParser implements ConfigSectionParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"advancements", "advancement"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.ADVANCEMENT;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            if (advancements.containsKey(id)) {
                TranslationManager.instance().log("warning.config.advancement.duplicated", path.toString(), id.toString());
                return;
            }
            JsonElement jsonTree = GsonHelper.get().toJsonTree(section);
            FastNMS.INSTANCE.registerAdvancement(id.decompose(), jsonTree);
            advancements.put(id, jsonTree);
        }
    }
}
