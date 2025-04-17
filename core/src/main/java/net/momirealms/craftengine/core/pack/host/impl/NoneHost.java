package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoneHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    public static final NoneHost INSTANCE = new NoneHost();

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        return CompletableFuture.completedFuture(null);
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
