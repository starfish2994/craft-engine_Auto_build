package net.momirealms.craftengine.core.pack.conflict.resolution;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Key;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergePackMcMetaResolution implements Resolution {
    public static final Factory FACTORY = new Factory();
    private final String description;

    public MergePackMcMetaResolution(String description) {
        this.description = description;
    }




    @SuppressWarnings("unchecked")
    public static void mergeMcmeta(Path existing, Path conflict, String customDescription) throws IOException {
        Gson gson = new Gson();

        Map<String, Object> existingData = gson.fromJson(new FileReader(existing.toFile()), new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> conflictData = gson.fromJson(new FileReader(conflict.toFile()), new TypeToken<Map<String, Object>>() {}.getType());

        Map<String, Object> merged = (Map<String, Object>) mergeValues(existingData, conflictData);

        processPackField(
                merged, existingData, conflictData,
                gson.fromJson(
                        AdventureHelper.miniMessageToJson(customDescription),
                        new TypeToken<Map<String, Object>>() {}.getType()
                )
        );

        Object cleaned = cleanEmpty(merged);

        try (FileWriter writer = new FileWriter(existing.toFile())) {
            gson.toJson(cleaned, writer);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object mergeValues(Object v1, Object v2) {
        if (v1 instanceof Map && v2 instanceof Map) {
            Map<String, Object> merged = new HashMap<>();
            ((Map<String, Object>) v1).forEach((k, val) ->
                    merged.put(k, ((Map<?, ?>) v2).containsKey(k) ? mergeValues(val, ((Map<?, ?>) v2).get(k)) : val));
            ((Map<String, Object>) v2).forEach(merged::putIfAbsent);
            return merged;
        }
        if (v1 instanceof List && v2 instanceof List) {
            List<Object> mergedList = new ArrayList<>((List<?>) v2);
            mergedList.addAll((List<?>) v1);
            return mergedList;
        }
        return v2 != null ? v2 : v1;
    }

    @SuppressWarnings("unchecked")
    private static Object cleanEmpty(Object data) {
        if (data instanceof Map) {
            Map<String, Object> map = new HashMap<>((Map<String, Object>) data);
            map.values().removeIf(MergePackMcMetaResolution::isEmpty);
            map.replaceAll((k, v) -> cleanEmpty(v));
            return map.isEmpty() ? null : map;
        }
        if (data instanceof List) {
            List<Object> list = new ArrayList<>((List<Object>) data);
            list.removeIf(MergePackMcMetaResolution::isEmpty);
            list.replaceAll(MergePackMcMetaResolution::cleanEmpty);
            return list.isEmpty() ? null : list;
        }
        return data;
    }

    private static boolean isEmpty(Object value) {
        return value == null ||
                (value instanceof Map && ((Map<?, ?>) value).isEmpty()) ||
                (value instanceof List && ((List<?>) value).isEmpty()) ||
                (value instanceof String && ((String) value).isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static void processPackField(Map<String, Object> merged, Map<String, Object> data1, Map<String, Object> data2, Object customDescription) {
        if (merged.containsKey("pack")) {
            Map<String, Object> pack = (Map<String, Object>) merged.get("pack");

            pack.put("pack_format", Math.max(
                    getPackFormat(data1),
                    getPackFormat(data2)
            ));

            processSupportedFormats(pack, data1, data2);

            if (customDescription != null && !isEmpty(customDescription)) {
                pack.put("description", customDescription);
            } else {
                String desc1 = getDescription(data1);
                String desc2 = getDescription(data2);
                String mergedDesc = desc1.isEmpty() ? desc2 : desc2.isEmpty() ? desc1 : desc1 + "\n" + desc2;
                if (!mergedDesc.isEmpty()) pack.put("description", mergedDesc);
            }
        }
    }

    private static int getPackFormat(Map<String, Object> data) {
        if (data.containsKey("pack") && ((Map<?, ?>) data.get("pack")).containsKey("pack_format")) {
            return ((Number) ((Map<?, ?>) data.get("pack")).get("pack_format")).intValue();
        }
        return 0;
    }

    private static void processSupportedFormats(Map<String, Object> pack, Map<String, Object> data1, Map<String, Object> data2) {
        Object sf1 = getSupportedFormats(data1);
        Object sf2 = getSupportedFormats(data2);

        if (sf1 != null || sf2 != null) {
            Object mergedSf = mergeValues(sf1, sf2);
            int[] minMax = parseSupportedFormats(mergedSf);
            Map<String, Integer> supported = new HashMap<>();
            supported.put("min_inclusive", minMax[0]);
            supported.put("max_inclusive", minMax[1]);
            pack.put("supported_formats", supported);
        }
    }

    private static Object getSupportedFormats(Map<String, Object> data) {
        if (data.containsKey("pack") && ((Map<?, ?>) data.get("pack")).containsKey("supported_formats")) {
            return ((Map<?, ?>) data.get("pack")).get("supported_formats");
        }
        return null;
    }

    private static int[] parseSupportedFormats(Object supported) {
        if (supported instanceof Number) {
            int val = ((Number) supported).intValue();
            return new int[]{val, val};
        }
        if (supported instanceof List<?> list) {
            return new int[]{
                    getNumberValue(list.getFirst()),
                    getNumberValue(list.getLast())
            };
        }
        if (supported instanceof Map<?, ?> map) {
            return new int[]{
                    getNumberValue(map.get("min_inclusive")),
                    getNumberValue(map.get("max_inclusive"))
            };
        }
        return new int[]{0, 0};
    }

    private static int getNumberValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private static String getDescription(Map<String, Object> data) {
        if (data.containsKey("pack") && ((Map<?, ?>) data.get("pack")).containsKey("description")) {
            Object desc = ((Map<?, ?>) data.get("pack")).get("description");
            return desc instanceof String ? (String) desc : "";
        }
        return "";
    }

    @Override
    public void run(Path existing, Path conflict) {
        try {
            mergeMcmeta(existing, conflict, description);
        } catch (IOException e) {
            CraftEngine.instance().logger().severe("Failed to merge pack.mcmeta when resolving file conflicts", e);
        }
    }

    @Override
    public Key type() {
        return Resolutions.MERGE_PACK_MCMETA;
    }

    public static class Factory implements ResolutionFactory {
        @Override
        public Resolution create(Map<String, Object> arguments) {
            String description = (String) arguments.getOrDefault("description", "<gray>CraftEngine ResourcePack");
            return new MergePackMcMetaResolution(description);
        }
    }
}
