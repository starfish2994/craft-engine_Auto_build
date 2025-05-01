package net.momirealms.craftengine.core.pack.conflict;

import net.momirealms.craftengine.core.util.context.CommonContext;
import net.momirealms.craftengine.core.util.context.ContextHolder;

import java.nio.file.Path;

public class PathContext extends CommonContext {
    private final Path path;

    public PathContext(ContextHolder holder, Path path) {
        super(holder);
        this.path = path;
    }

    public Path path() {
        return path;
    }

    public static PathContext of(Path path) {
        return new PathContext(ContextHolder.EMPTY, path);
    }
}
