package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SelfHost implements ResourcePackHost {

    public SelfHost(String ip, int port) {
        SelfHostHttpServer.instance().setIp(ip);
        SelfHostHttpServer.instance().updatePort(port);
    }

    @Override
    public CompletableFuture<ResourcePackDownloadData> requestResourcePackDownloadLink(UUID player) {
        return CompletableFuture.completedFuture(SelfHostHttpServer.instance().generateOneTimeUrl(player));
    }

    @Override
    public ResourcePackDownloadData getResourcePackDownloadLink(UUID player) {
        return SelfHostHttpServer.instance().getCachedOneTimeUrl(player);
    }

    @Override
    public CompletableFuture<Boolean> upload(Path resourcePackPath) {
        SelfHostHttpServer.instance().setResourcePackPath(resourcePackPath);
        return CompletableFuture.completedFuture(true);
    }
}
