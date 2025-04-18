package net.momirealms.craftengine.core.pack.host.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.HashUtils;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DropboxHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final String accessToken;
    private final String uploadPath;
    private final ProxySelector proxy;

    private String url;
    private String sha1;
    private UUID uuid;

    public DropboxHost(String accessToken, String uploadPath, ProxySelector proxy) {
        this.accessToken = accessToken;
        this.uploadPath = uploadPath;
        this.proxy = proxy;
        readCacheFromDisk();
    }

    public void readCacheFromDisk() {
        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("dropbox.cache");
        if (!Files.exists(cachePath)) return;

        try (InputStream is = Files.newInputStream(cachePath)) {
            Map<String, String> cache = GsonHelper.get().fromJson(
                    new InputStreamReader(is),
                    new TypeToken<Map<String, String>>(){}.getType()
            );

            this.url = cache.get("url");
            this.sha1 = cache.get("sha1");

            String uuidString = cache.get("uuid");
            if (uuidString != null && !uuidString.isEmpty()) {
                this.uuid = UUID.fromString(uuidString);
            }

            CraftEngine.instance().logger().info("[DropBox] Loaded cached resource pack info");
        } catch (Exception e) {
            CraftEngine.instance().logger().warn(
                    "[DropBox] Failed to read cache file: " + e.getMessage());
        }
    }

    public void saveCacheToDisk() {
        Map<String, String> cache = new HashMap<>();
        cache.put("url", this.url);
        cache.put("sha1", this.sha1);
        cache.put("uuid", this.uuid != null ? this.uuid.toString() : "");

        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("dropbox.cache");
        try {
            Files.writeString(
                    cachePath,
                    GsonHelper.get().toJson(cache),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            CraftEngine.instance().logger().warn(
                    "[DropBox] Failed to save cache: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        if (url == null) return CompletableFuture.completedFuture(Collections.emptyList());
        return CompletableFuture.completedFuture(List.of(ResourcePackDownloadData.of(url, uuid, sha1)));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            String sha1 = HashUtils.calculateLocalFileSha1(resourcePackPath);
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                JsonObject apiArg = new JsonObject();
                apiArg.addProperty("path", uploadPath);
                apiArg.addProperty("mode", "overwrite");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://content.dropboxapi.com/2/files/upload"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/octet-stream")
                        .header("Dropbox-API-Arg", apiArg.toString())
                        .POST(HttpRequest.BodyPublishers.ofFile(resourcePackPath))
                        .build();

                long uploadStart = System.currentTimeMillis();
                CraftEngine.instance().logger().info("[DropBox] Starting file upload...");
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            long uploadTime = System.currentTimeMillis() - uploadStart;
                            CraftEngine.instance().logger().info(
                                    "[DropBox] Upload request completed in " + uploadTime + "ms");
                            if (response.statusCode() == 200) {
                                this.sha1 = sha1;
                                this.uuid = UUID.nameUUIDFromBytes(sha1.getBytes(StandardCharsets.UTF_8));
                                this.url = getDownloadUrl();
                                saveCacheToDisk();
                                future.complete(null);
                                return;
                            }
                            CraftEngine.instance().logger().warn(
                                    "[DropBox] Upload resource pack failed: " + response.body());
                            future.completeExceptionally(new RuntimeException(response.body()));
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().warn("[DropBox] Upload resource pack failed", ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            } catch (FileNotFoundException e) {
                CraftEngine.instance().logger().warn("[DropBox] Failed to upload resource pack: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private String getDownloadUrl() {
        try (HttpClient client = HttpClient.newBuilder().proxy(this.proxy).build()) {
            try {
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("path", this.uploadPath);
                JsonObject settingsJson = new JsonObject();
                settingsJson.addProperty("requested_visibility", "public");
                requestJson.add("settings", settingsJson);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.dropboxapi.com/2/sharing/create_shared_link_with_settings"))
                        .header("Authorization", "Bearer " + this.accessToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 409) {
                    JsonObject listJson = new JsonObject();
                    listJson.addProperty("path", this.uploadPath);
                    HttpRequest listLinksRequest = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.dropboxapi.com/2/sharing/list_shared_links"))
                            .header("Authorization", "Bearer " + this.accessToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(listJson.toString()))
                            .build();
                    HttpResponse<String> listResponse = client.send(listLinksRequest, HttpResponse.BodyHandlers.ofString());
                    if (listResponse.statusCode() == 200) {
                        JsonObject responseJson = GsonHelper.parseJsonToJsonObject(listResponse.body());
                        JsonArray links = responseJson.getAsJsonArray("links");
                        if (!links.isEmpty()) {
                            return links.get(0).getAsJsonObject().get("url").getAsString().replace("dl=0", "dl=1");
                        }
                    }
                } else if (response.statusCode() != 200) {
                    CraftEngine.instance().logger().warn("[DropBox] Failed to get download url: " + response.body());
                    return null;
                }
                JsonObject jsonData = GsonHelper.parseJsonToJsonObject(response.body());
                return jsonData.getAsJsonPrimitive("url").getAsString().replace("dl=0", "dl=1");
            } catch (IOException | InterruptedException e) {
                CraftEngine.instance().logger().warn("[DropBox] Failed to get download url: " + e.getMessage());
                return null;
            }
        }
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            String accessToken = (String) arguments.get("access-token");
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Missing 'access-token' for DropboxHost");
            }
            String uploadPath = (String) arguments.getOrDefault("upload-path", "/resource_pack.zip");
            ProxySelector proxy = MiscUtils.getProxySelector(arguments.get("proxy"));
            return new DropboxHost(accessToken, uploadPath, proxy);
        }
    }
}
