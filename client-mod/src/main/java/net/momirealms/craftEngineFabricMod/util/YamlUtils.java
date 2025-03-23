package net.momirealms.craftEngineFabricMod.util;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlUtils {

    public static Map<String, String> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = YamlUtils.class.getClassLoader()
                .getResourceAsStream("mappings.yml");
        return yaml.load(inputStream);
    }

    public static @Nullable String split(String str) {
        int colonIndex = str.indexOf(':');
        int bracketIndex = str.indexOf('[');
        if (colonIndex == -1 && bracketIndex == -1) return null;
        int start = (colonIndex != -1) ? colonIndex + 1 : 0;
        int end = (bracketIndex != -1) ? bracketIndex : str.length();
        if (start > end) start = end;
        return str.substring(start, end);
    }
}
