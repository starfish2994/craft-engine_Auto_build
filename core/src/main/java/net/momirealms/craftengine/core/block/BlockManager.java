package net.momirealms.craftengine.core.block;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.model.generation.ModelGenerator;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.util.Key;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface BlockManager extends Manageable, ModelGenerator {

    ConfigSectionParser parser();

    Collection<ModelGeneration> modelsToGenerate();

    Map<Key, Map<String, JsonElement>> blockOverrides();

    Map<Key, JsonElement> modBlockStates();

    Map<Key, CustomBlock> blocks();

    Optional<CustomBlock> getBlock(Key key);

    Collection<Suggestion> cachedSuggestions();

    Map<Key, Key> soundMapper();
}
