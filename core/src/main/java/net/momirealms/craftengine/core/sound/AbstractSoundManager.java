package net.momirealms.craftengine.core.sound;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractSoundManager implements SoundManager {
    protected static final Set<Key> VANILLA_SOUND_EVENTS = new HashSet<>();
    protected final CraftEngine plugin;
    protected final Map<Key, SoundEvent> byId = new HashMap<>();
    protected final Map<String, List<SoundEvent>> byNamespace = new HashMap<>();
    protected final Map<Key, JukeboxSong> songs = new HashMap<>();
    protected final SoundParser soundParser;
    protected final SongParser songParser;

    public AbstractSoundManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.soundParser = new SoundParser();
        this.songParser = new SongParser();
    }

    @Override
    public boolean isVanillaSoundEvent(Key key) {
        return VANILLA_SOUND_EVENTS.contains(key);
    }

    @Override
    public ConfigSectionParser[] parsers() {
        return new ConfigSectionParser[] { this.soundParser, this.songParser };
    }

    @Override
    public void unload() {
        this.byId.clear();
        this.byNamespace.clear();
    }

    @Override
    public void runDelayedSyncTasks() {
        if (!VersionHelper.isOrAbove1_21()) return;
        this.registerSongs(this.songs);
    }

    @Override
    public Map<Key, SoundEvent> sounds() {
        return Collections.unmodifiableMap(this.byId);
    }

    public Map<String, List<SoundEvent>> soundsByNamespace() {
        return Collections.unmodifiableMap(this.byNamespace);
    }

    protected abstract void registerSongs(Map<Key, JukeboxSong> songs);

    public class SongParser implements ConfigSectionParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"jukebox_songs", "song", "songs", "jukebox", "jukebox_song"};

        @Override
        public int loadingSequence() {
            return LoadingSequence.JUKEBOX_SONG;
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            if (AbstractSoundManager.this.songs.containsKey(id)) {
                throw new LocalizedResourceConfigException("warning.config.jukebox_song.duplicated", path, id);
            }
            String sound = (String) section.get("sound");
            if (sound == null) {
                throw new LocalizedResourceConfigException("warning.config.jukebox_song.lack_sound", path, id);
            }
            Component description = AdventureHelper.miniMessage().deserialize(section.getOrDefault("description", "").toString());
            float length = MiscUtils.getAsFloat(section.get("length"));
            int comparatorOutput = MiscUtils.getAsInt(section.getOrDefault("comparator-output", 15));
            JukeboxSong song = new JukeboxSong(Key.of(sound), description, length, comparatorOutput, MiscUtils.getAsFloat(section.getOrDefault("range", 32f)));
            AbstractSoundManager.this.songs.put(id, song);
        }
    }

    public class SoundParser implements ConfigSectionParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"sounds", "sound"};

        @Override
        public int loadingSequence() {
            return LoadingSequence.SOUND;
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            if (AbstractSoundManager.this.byId.containsKey(id)) {
                throw new LocalizedResourceConfigException("warning.config.sound.duplicated", path, id);
            }
            boolean replace = (boolean) section.getOrDefault("replace", false);
            String subtitle = (String) section.get("subtitle");
            List<?> soundList = (List<?>) section.get("sounds");
            if (soundList == null) {
                throw new LocalizedResourceConfigException("warning.config.sound.lack_sounds", path, id);
            }
            List<Sound> sounds = new ArrayList<>();
            for (Object sound : soundList) {
                if (sound instanceof String soundPath) {
                    sounds.add(Sound.path(soundPath));
                } else if (sound instanceof Map<?,?> map) {
                    sounds.add(Sound.SoundFile.fromMap(MiscUtils.castToMap(map, false)));
                }
            }
            SoundEvent event = new SoundEvent(id, replace, subtitle, sounds);
            AbstractSoundManager.this.byId.put(id, event);
            AbstractSoundManager.this.byNamespace.computeIfAbsent(id.namespace(), k -> new ArrayList<>()).add(event);
        }
    }
}
