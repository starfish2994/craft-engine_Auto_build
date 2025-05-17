package net.momirealms.craftengine.core.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() {}

    public static String pathWithoutExtension(String path) {
        int i = path.lastIndexOf('.');
        return i == -1 ? path : path.substring(0, i);
    }

    public static void createDirectoriesSafe(Path path) throws IOException {
        Files.createDirectories(Files.exists(path) ? path.toRealPath() : path);
    }

    public static List<Path> getYmlConfigsDeeply(Path configFolder) {
        if (!Files.exists(configFolder)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(configFolder)) {
            return stream.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse directory: " + configFolder, e);
        }
    }
}
