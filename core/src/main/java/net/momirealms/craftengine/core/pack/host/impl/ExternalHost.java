package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.pack.host.ResourcePackHosts;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ExternalHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final ResourcePackDownloadData downloadData;

    public ExternalHost(ResourcePackDownloadData downloadData) {
        this.downloadData = downloadData;
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        return CompletableFuture.completedFuture(List.of(this.downloadData));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean canUpload() {
        return false;
    }

    @Override
    public Key type() {
        return ResourcePackHosts.EXTERNAL;
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            String url = (String) arguments.get("url");
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("'url' cannot be empty for external host");
            }
            String uuid = (String) arguments.get("uuid");
            if (uuid == null || uuid.isEmpty()) {
                uuid = UUID.nameUUIDFromBytes(url.getBytes()).toString();
            }
            UUID hostUUID = UUID.fromString(uuid);
            String sha1 = (String) arguments.get("sha1");
            if (sha1 == null) {
                sha1 = "";
            }
            return new ExternalHost(new ResourcePackDownloadData(url, hostUUID, sha1));
        }
    }
}
