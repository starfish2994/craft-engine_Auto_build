package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.pack.host.ResourcePackHosts;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SelfHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private static final SelfHost INSTANCE = new SelfHost();

    public SelfHost() {
        SelfHostHttpServer.instance().readResourcePack(Config.fileToUpload());
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        ResourcePackDownloadData data = SelfHostHttpServer.instance().generateOneTimeUrl();
        if (data == null) return CompletableFuture.completedFuture(List.of());
        return CompletableFuture.completedFuture(List.of(data));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try {
                SelfHostHttpServer.instance().readResourcePack(resourcePackPath);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean canUpload() {
        return true;
    }

    @Override
    public Key type() {
        return ResourcePackHosts.SELF;
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            SelfHostHttpServer selfHostHttpServer = SelfHostHttpServer.instance();
            String ip = (String) arguments.get("ip");
            if (ip == null) {
                throw new LocalizedException("warning.config.host.self.lack_ip");
            }
            int port = MiscUtils.getAsInt(arguments.getOrDefault("port", 8163));
            if (port < 0 || port > 65535) {
                throw new LocalizedException("warning.config.host.self.invalid_port", String.valueOf(port));
            }
            boolean oneTimeToken = (boolean) arguments.getOrDefault("one-time-token", true);
            String protocol = (String) arguments.getOrDefault("protocol", "http");
            boolean denyNonMinecraftRequest = (boolean) arguments.getOrDefault("deny-non-minecraft-request", true);
            Map<String, Object> rateMap = MiscUtils.castToMap(arguments.get("rate-map"), true);
            int maxRequests = 5;
            int resetInterval = 20_000;
            if (rateMap != null) {
                maxRequests = MiscUtils.getAsInt(rateMap.getOrDefault("max-requests", 5));
                resetInterval = MiscUtils.getAsInt(rateMap.getOrDefault("reset-interval", 20)) * 1000;
            }
            selfHostHttpServer.updateProperties(ip, port, denyNonMinecraftRequest, protocol, maxRequests, resetInterval, oneTimeToken);
            return INSTANCE;
        }
    }
}
