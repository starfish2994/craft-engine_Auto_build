package net.momirealms.craftengine.bukkit.block.worldedit;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitBlockRegistry;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockType;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockStateParser;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.incendo.cloud.suggestion.Suggestion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldEditHook implements Listener {
    private static final Field field$BlockType$blockMaterial;
    private final Map<String, SuggestionHandler> handlers = ImmutableMap.of(
            "//set", SuggestionHandler.of(1),
        "//replace", SuggestionHandler.of(1, 2)
    );

    static {
        WorldEdit.getInstance().getEventBus().register(new WorldEditHook());
        field$BlockType$blockMaterial = ReflectionUtils.getDeclaredField(BlockType.class, "blockMaterial");
    }

    public static void register(Key id) throws ReflectiveOperationException {
        BlockType blockType = new BlockType(id.toString(), blockState -> blockState);
        field$BlockType$blockMaterial.set(blockType, LazyReference.from(() -> new BukkitBlockRegistry.BukkitBlockMaterial(null, Material.STONE)));
        BlockType.REGISTRY.register(id.toString(), blockType);
    }

    public WorldEditHook() {
        Bukkit.getPluginManager().registerEvents(this, BukkitCraftEngine.instance().bootstrap());
    }

    @Subscribe
    public void onSuggestion(CommandSuggestionEvent event) {
        String input = event.getArguments();
        String command = input.substring(0, input.indexOf(" "));

        SuggestionHandler handler = handlers.get(command);
        if (handler == null || !handler.matches(input)) return;
        int start = input.lastIndexOf(" ") + 1;
        int end = input.length();

        if (start == end) {
            List<Substring> suggestions = BukkitCraftEngine.instance().blockManager().cachedNamespaces()
                    .stream()
                    .map(ns -> Substring.wrap(ns + ":", start, end))
                    .collect(Collectors.toList());
            suggestions.addAll(event.getSuggestions());
            event.setSuggestions(suggestions);
            return;
        }

        String last = input.substring(start, end);
        List<Substring> suggestions = new ArrayList<>();
        for (Suggestion s : BukkitCraftEngine.instance().blockManager().cachedSuggestions()) {
            String id = s.suggestion();
            if (id.startsWith(last))
                suggestions.add(Substring.wrap(id, start, end));
        }
        suggestions.addAll(event.getSuggestions());
        event.setSuggestions(suggestions);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("//")) return;

        Collection<String> cachedNamespaces = BukkitCraftEngine.instance().blockManager().cachedNamespaces();

        String[] args = message.split(" ");
        boolean modified = false;

        for (int i = 1; i < args.length; i++) {
            String token = args[i];

            int colon = token.indexOf(':');
            if (colon == -1) continue;

            String namespace = token.substring(0, colon);
            if (!cachedNamespaces.contains(namespace)) continue;

            ImmutableBlockState state = BlockStateParser.deserialize(token);
            if (state == null) continue;

            String internalId = BlockStateUtils.getBlockOwnerIdFromState(state.customBlockState().handle()).toString();
            args[i] = internalId;
            modified = true;
        }

        if (modified) {
            event.setMessage(String.join(" ", args));
        }
    }
}
