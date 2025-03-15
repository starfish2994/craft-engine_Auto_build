package net.momirealms.craftengine.core.pack.conflict.resolution;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class MergePackMcMetaResolution implements Resolution {
    public static final Factory FACTORY = new Factory();
    private final String description;

    public MergePackMcMetaResolution(String description) {
        this.description = description;
    }

    public static void mergeMcmeta(Path existing, Path conflict, JsonObject customDescription) throws IOException {
        // 读取并解析JSON文件
        JsonObject existingJson = GsonHelper.readJsonFile(existing).getAsJsonObject();
        JsonObject conflictJson = GsonHelper.readJsonFile(conflict).getAsJsonObject();

        // 合并根对象
        JsonObject merged = GsonHelper.deepMerge(existingJson, conflictJson);

        // 处理pack字段的特殊逻辑
        processPackField(merged, existingJson, conflictJson, customDescription);

        // 写回文件
        GsonHelper.writeJsonFile(merged, existing);
    }

    private static void processPackField(JsonObject merged, JsonObject data1, JsonObject data2, JsonObject customDescription) {
        JsonObject pack1 = data1.getAsJsonObject("pack");
        JsonObject pack2 = data2.getAsJsonObject("pack");

        JsonObject mergedPack = new JsonObject();

        // 合并pack_format
        int packFormat1 = pack1 != null && pack1.has("pack_format") ? pack1.get("pack_format").getAsInt() : 0;
        int packFormat2 = pack2 != null && pack2.has("pack_format") ? pack2.get("pack_format").getAsInt() : 0;
        mergedPack.addProperty("pack_format", Math.max(packFormat1, packFormat2));

        // 处理supported_formats
        JsonElement sf1 = pack1 != null ? pack1.get("supported_formats") : null;
        JsonElement sf2 = pack2 != null ? pack2.get("supported_formats") : null;
        if (sf1 != null || sf2 != null) {
            JsonElement mergedSf = mergeSupportedFormats(sf1, sf2);
            mergedPack.add("supported_formats", mergedSf);
        }

        // 处理description
        mergedPack.add("description", customDescription);
        merged.add("pack", mergedPack);
    }

    private static JsonElement mergeSupportedFormats(JsonElement sf1, JsonElement sf2) {
        final int[] min = {Integer.MAX_VALUE};
        final int[] max = {Integer.MIN_VALUE};

        parseSupportedFormats(sf1, (m, n) -> {
            min[0] = Math.min(min[0], m);
            max[0] = Math.max(max[0], n);
        });

        parseSupportedFormats(sf2, (m, n) -> {
            min[0] = Math.min(min[0], m);
            max[0] = Math.max(max[0], n);
        });

        JsonObject result = new JsonObject();
        result.addProperty("min_inclusive", min[0]);
        result.addProperty("max_inclusive", max[0]);
        return result;
    }

    private static void parseSupportedFormats(JsonElement element, MinMaxConsumer consumer) {
        if (element == null) return;

        if (element.isJsonPrimitive()) {
            int value = element.getAsInt();
            consumer.accept(value, value);
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (!array.isEmpty()) {
                int first = array.get(0).getAsInt();
                int last = array.get(array.size() - 1).getAsInt();
                consumer.accept(first, last);
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            int m = obj.has("min_inclusive") ? obj.get("min_inclusive").getAsInt() : 0;
            int n = obj.has("max_inclusive") ? obj.get("max_inclusive").getAsInt() : 0;
            consumer.accept(m, n);
        }
    }

    private interface MinMaxConsumer {
        void accept(int min, int max);
    }

    @Override
    public void run(Path existing, Path conflict) {
        try {
            mergeMcmeta(existing, conflict, JsonParser.parseString(AdventureHelper.miniMessageToJson(this.description)).getAsJsonObject());
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
