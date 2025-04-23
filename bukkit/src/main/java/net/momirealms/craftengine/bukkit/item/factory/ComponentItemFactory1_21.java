package net.momirealms.craftengine.bukkit.item.factory;

import com.saicone.rtag.data.ComponentType;
import net.momirealms.craftengine.bukkit.item.ComponentItemWrapper;
import net.momirealms.craftengine.bukkit.item.ComponentTypes;
import net.momirealms.craftengine.core.item.JukeboxPlayable;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComponentItemFactory1_21 extends ComponentItemFactory1_20_5 {

    public ComponentItemFactory1_21(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    protected Optional<JukeboxPlayable> jukeboxSong(ComponentItemWrapper item) {
        if (!item.hasComponent(ComponentTypes.JUKEBOX_PLAYABLE)) return Optional.empty();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) ComponentType.encodeJava(
                        ComponentTypes.JUKEBOX_PLAYABLE,
                        item.getComponent(ComponentTypes.JUKEBOX_PLAYABLE)
                ).orElse(null);
        if (map == null) return Optional.empty();
        return Optional.of(new JukeboxPlayable((String) map.get("song"), (boolean) map.getOrDefault("show_in_tooltip", true)));
    }

    @Override
    protected void jukeboxSong(ComponentItemWrapper item, JukeboxPlayable data) {
        item.setJavaComponent(ComponentTypes.JUKEBOX_PLAYABLE, Map.of(
                "song", data.song(),
                "show_in_tooltip", true
        ));
    }
}
