package net.momirealms.craftengine.core.pack.host.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SelfHostHttpServer {
    private static SelfHostHttpServer instance;
    private Cache<UUID, String> oneTimePackUrls = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();
    private HttpServer server;
    private final ConcurrentHashMap<String, IpAccessRecord> ipAccessMap = new ConcurrentHashMap<>();
    private int rateLimit = 1;
    private long rateLimitInterval = 1000;
    private String ip = "localhost";
    private int port = -1;
    private Path resourcePackPath;
    private String packHash;
    private UUID packUUID;

    public String generateOneTimeUrl(UUID player) {

    }

    public String url() {
        return Config.hostProtocol() + "://" + ip + ":" + port + "/";
    }

    public void setResourcePackPath(Path resourcePackPath) {
        this.resourcePackPath = resourcePackPath;
    }

    private void calculateHash() {
        if (Files.exists(this.resourcePackPath)) {
            try {
                this.packHash = computeSHA1(this.resourcePackPath);
                this.packUUID = UUID.nameUUIDFromBytes(this.packHash.getBytes(StandardCharsets.UTF_8));
            } catch (IOException | NoSuchAlgorithmException e) {
                CraftEngine.instance().logger().severe("Error calculating resource pack hash", e);
            }
        } else {
            this.packHash = "";
            this.packUUID = UUID.nameUUIDFromBytes("EMPTY".getBytes(StandardCharsets.UTF_8));
        }
    }

    public void updatePort(int port) {
        if (port == this.port) {
            return;
        }
        if (server != null) {
            disable();
        }
        this.port = port;
        try {
            server = HttpServer.create(new InetSocketAddress("::", port), 0);
            server.createContext("/", new ResourcePackHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            CraftEngine.instance().logger().info("HTTP resource pack server running on port: " + port);
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Failed to start HTTP server", e);
        }
    }

    public void disable() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public boolean isAlive() {
        return server != null;
    }

    public static SelfHostHttpServer instance() {
        if (instance == null) {
            instance = new SelfHostHttpServer();
        }
        return instance;
    }

    public void setRateLimit(int rateLimit, long rateLimitInterval, TimeUnit timeUnit) {
        this.rateLimit = rateLimit;
        this.rateLimitInterval = timeUnit.toMillis(rateLimitInterval);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    private String computeSHA1(Path path) throws IOException, NoSuchAlgorithmException {
        InputStream file = Files.newInputStream(path);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = file.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        file.close();

        StringBuilder hexString = new StringBuilder(40);
        for (byte b : digest.digest()) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private class ResourcePackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (Config.denyNonMinecraftRequest()) {
                String userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
                if (userAgent == null || !userAgent.startsWith("Minecraft Java/")) {
                    CraftEngine.instance().debug(() -> "Blocked non-Minecraft Java client. User-Agent: " + userAgent);
                    sendError(exchange, 403);
                    return;
                }
            }

            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            IpAccessRecord record = ipAccessMap.compute(clientIp, (k, v) -> {
                long currentTime = System.currentTimeMillis();
                if (v == null || currentTime - v.lastAccessTime > rateLimitInterval) {
                    return new IpAccessRecord(currentTime, 1);
                } else {
                    v.accessCount++;
                    return v;
                }
            });

            if (record.accessCount > rateLimit) {
                CraftEngine.instance().debug(() -> "Rate limit exceeded for IP: " + clientIp);
                sendError(exchange, 429);
                return;
            }

            if (!Files.exists(resourcePackPath)) {
                CraftEngine.instance().logger().warn("ResourcePack not found: " + resourcePackPath);
                sendError(exchange, 404);
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(Files.size(resourcePackPath)));
            exchange.sendResponseHeaders(200, Files.size(resourcePackPath));

            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(resourcePackPath, os);
            } catch (IOException e) {
                CraftEngine.instance().logger().warn("Failed to send pack", e);
            }
        }

        private void sendError(HttpExchange exchange, int code) throws IOException {
            exchange.sendResponseHeaders(code, 0);
            exchange.getResponseBody().close();
        }
    }

    private static class IpAccessRecord {
        long lastAccessTime;
        int accessCount;

        IpAccessRecord(long lastAccessTime, int accessCount) {
            this.lastAccessTime = lastAccessTime;
            this.accessCount = accessCount;
        }
    }
}