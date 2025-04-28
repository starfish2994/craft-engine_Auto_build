package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public class FilenameMatcher implements PathMatcher {
    public static final Factory FACTORY = new Factory();
    private final String name;

    public FilenameMatcher(String name) {
        this.name = name;
    }

    @Override
    public boolean test(Path path) {
        String fileName = String.valueOf(path.getFileName());
        return fileName.equals(name);
    }

    @Override
    public Key type() {
        return PathMatchers.FILENAME;
    }

    public static class Factory implements PathMatcherFactory {

        @Override
        public PathMatcher create(Map<String, Object> arguments) {
            Object name = arguments.get("name");
            if (name == null) {
                throw new LocalizedResourceConfigException("warning.config.conflict_matcher.filename.missing_name");
            }
            return new FilenameMatcher(name.toString());
        }
    }
}
