package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class StringKeyConstructor extends SafeConstructor {
    private final Path path;

    public StringKeyConstructor(Path path, LoaderOptions loaderOptions) {
        super(loaderOptions);
        this.path = path;
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
        Map<Object, Object> map = new LinkedHashMap<>();
        for (NodeTuple tuple : node.getValue()) {
            Node keyNode = tuple.getKeyNode();
            Node valueNode = tuple.getValueNode();
            String key = constructScalar((ScalarNode) keyNode);
            Object value = constructObject(valueNode);
            Object previous = map.put(key, value);
            if (previous != null) {
                TranslationManager.instance().log("warning.config.yaml.duplicated_key", this.path.toAbsolutePath().toString(), key, String.valueOf(node.getStartMark().getLine() + 1));
            }
        }
        return map;
    }
}
