
package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.VersionHelper;
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
    private static final String VERSION_PREFIX = "$$";
    private static final String DEEP_KEY_SEPARATOR = "::";

    public StringKeyConstructor(Path path, LoaderOptions loaderOptions) {
        super(loaderOptions);
        this.path = path;
    }

    private boolean isVersionMatch(String versionSpec) {
        int index = versionSpec.indexOf('~');
        // 没有范围值
        if (index == -1) {
            char firstChar = versionSpec.charAt(0);
            if (firstChar == '>') {
                int version = VersionHelper.parseVersionToInteger(versionSpec);
                return versionSpec.charAt(1) == '=' ? VersionHelper.version() >= version : VersionHelper.version() > version;
            } else if (firstChar == '<') {
                int version = VersionHelper.parseVersionToInteger(versionSpec);
                return versionSpec.charAt(1) == '=' ? VersionHelper.version() <= version : VersionHelper.version() < version;
            } else {
                return VersionHelper.parseVersionToInteger(versionSpec) == VersionHelper.version();
            }
        } else {
            int min = VersionHelper.parseVersionToInteger(versionSpec.substring(0, index));
            int max = VersionHelper.parseVersionToInteger(versionSpec.substring(index + 1));
            return VersionHelper.version() >= min && VersionHelper.version() <= max;
        }
    }

    /**
     * Dispatcher: 决定一个节点是应该被解析为“版本化值”还是一个普通的Map。
     */
    @Override
    public Object constructObject(Node node) {
        if (node instanceof MappingNode mappingNode) {
            if (isValueSelectorNode(mappingNode)) {
                // 场景B: 这是一个值选择器，解析它以获得单个值
                return constructVersionedValue(mappingNode);
            }
        }
        // 对于所有其他情况 (包括需要合并的Map)，使用默认的构造逻辑
        // super.constructObject 会最终调用我们重写的 constructMapping
        return super.constructObject(node);
    }

    /**
     * 场景A (块合并与路径展开): 构造一个Map，同时处理其中的版本化块合并和 `::` 分隔的深层键。
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
        Map<Object, Object> map = new LinkedHashMap<>();
        for (NodeTuple tuple : node.getValue()) {
            Node keyNode = tuple.getKeyNode();
            if (!(keyNode instanceof ScalarNode)) continue;

            String key = constructScalar((ScalarNode) keyNode);
            Node valueNode = tuple.getValueNode();

            // 处理 版本化块 合并.
            if (key.startsWith(VERSION_PREFIX)) {
                String versionSpec = key.substring(VERSION_PREFIX.length());
                if (isVersionMatch(versionSpec)) {
                    if (valueNode instanceof MappingNode) {
                        // 将版本匹配的map内容合并到当前map
                        Map<Object, Object> versionedMap = constructMapping((MappingNode) valueNode);
                        mergeMap(map, versionedMap);
                    } else {
                        logWarning("versioned_key_not_a_map", key, valueNode);
                    }
                }
            }
            // 处理::分隔的键 ->  {a::b::c: value} 和 {a::b: {c: value}}.
            else if (key.contains(DEEP_KEY_SEPARATOR)) {
                processDeepKey(map, key, valueNode, keyNode);
            }
            // 处理正常的内容.
            else {
                Object value = constructObject(valueNode);

                // 检查是否需要与现有的Map合并
                Object existing = map.get(key);
                if (existing instanceof Map && value instanceof Map) {
                    // 如果已存在同名的Map（可能是由深层键创建的），则合并它们
                    mergeMap((Map<Object, Object>) existing, (Map<Object, Object>) value);
                } else {
                    // 否则正常设置
                    Object previous = map.put(key, value);

                    // 如果之前有值,
                    if (previous != null && !(previous instanceof Map)) {
                        logWarning("duplicated_key", key, keyNode);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 处理深层键的逻辑，支持完全深层键和部分深层键的合并
     */
    @SuppressWarnings("unchecked")
    private void processDeepKey(Map<Object, Object> rootMap, String key, Node valueNode, Node keyNode) {
        String[] parts = key.split(DEEP_KEY_SEPARATOR);
        Map<Object, Object> currentMap = rootMap;

        // 遍历除最后一个部分外的所有路径，创建或导航到嵌套的map
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object nextObject = currentMap.get(part);
            if (nextObject instanceof Map) {
                currentMap = (Map<Object, Object>) nextObject;
            } else {
                // 如果路径中存在一个非map的值，发出警告并覆盖它
                if (nextObject != null) {
                    logWarning("key_path_conflict", part, keyNode);
                }
                Map<Object, Object> newMap = new LinkedHashMap<>();
                currentMap.put(part, newMap);
                currentMap = newMap;
            }
        }

        // 处理最后一个部分
        String finalKey = parts[parts.length - 1];
        Object value = constructObject(valueNode);

        // 检查最终键是否需要合并
        Object existing = currentMap.get(finalKey);
        if (existing instanceof Map && value instanceof Map) {
            // 如果目标位置已经有一个Map，且新值也是Map，则合并它们
            mergeMap((Map<Object, Object>) existing, (Map<Object, Object>) value);
        } else {
            // 否则直接设置值
            Object previous = currentMap.put(finalKey, value);
            if (previous != null && !(previous instanceof Map)) {
                // 只有当之前的值不是Map时才报告重复键警告
                logWarning("duplicated_key", key, keyNode);
            }
        }
    }

    /**
     * 合并两个Map
     */
    @SuppressWarnings("unchecked")
    private void mergeMap(Map<Object, Object> target, Map<Object, Object> source) {
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            Object key = entry.getKey();
            Object sourceValue = entry.getValue();
            Object targetValue = target.get(key);

            // 如果都是Map，则递归合并.
            if (targetValue instanceof Map && sourceValue instanceof Map) {
                mergeMap((Map<Object, Object>) targetValue, (Map<Object, Object>) sourceValue);
                return;
            }

            // TODO 不能覆盖, 得想办法丢个异常.
            target.put(key, sourceValue);
        }
    }

    /**
     * 检查一个MappingNode是否是“值选择器”（即所有键都以 '$$' 开头）。
     */
    private boolean isValueSelectorNode(MappingNode node) {
        if (node.getValue().isEmpty()) {
            return false;
        }
        for (NodeTuple tuple : node.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode scalarNode) {
                String key = scalarNode.getValue();
                if (!key.startsWith(VERSION_PREFIX)) {
                    return false; // 发现一个普通键，因此它不是值选择器
                }
            } else {
                return false; // 键不是一个简单的字符串，不可能是值选择器
            }
        }
        return true; // 所有键都是版本化的
    }

    /**
     * 场景B (值选择): 从“值选择器”节点中解析出最终的单个值。
     */
    private Object constructVersionedValue(MappingNode node) {
        Object fallbackValue = null;
        Object matchedValue = null;
        // 遍历所有版本键，寻找匹配项
        for (NodeTuple tuple : node.getValue()) {
            String key = ((ScalarNode) tuple.getKeyNode()).getValue();
            String versionSpec = key.substring(VERSION_PREFIX.length());
            if ("fallback".equals(versionSpec)) {
                // 找到备用值，先存起来
                fallbackValue = constructObject(tuple.getValueNode());
                continue;
            }
            if (isVersionMatch(versionSpec)) {
                // 找到一个匹配项，因为YAML是顺序的，后面的会覆盖前面的
                matchedValue = constructObject(tuple.getValueNode());
            }
        }

        // 如果有精确匹配的值，则使用它；否则，使用备用值
        return matchedValue != null ? matchedValue : fallbackValue;
    }

    private void logWarning(String keyInLocale, String configKey, Node node) {
        if (this.path == null) return;
        TranslationManager.instance().log("warning.config.yaml." + keyInLocale,
                this.path.toAbsolutePath().toString(),
                configKey,
                String.valueOf(node.getStartMark().getLine() + 1));
    }
}