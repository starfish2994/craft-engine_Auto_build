package net.momirealms.craftengine.core.pack.host.impl;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CustomApiHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final String apiUrl;
    private final String authKey;
    private final Path localFilePath;

    public CustomApiHost(String apiUrl, String authKey, String localFilePath) {
        this.apiUrl = apiUrl;
        this.authKey = authKey;
        this.localFilePath = localFilePath == null ? null : Path.of(localFilePath);
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        CompletableFuture<List<ResourcePackDownloadData>> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/api/v1/get-download-link?uuid=" + player))
                        .header("Authorization", authKey)
                        .GET()
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                Map<String, Object> jsonData = parseJson(response.body());
                                String url = (String) jsonData.get("url");
                                String sha1 = (String) jsonData.get("sha1");
                                UUID uuid = UUID.fromString(sha1);
                                future.complete(List.of(new ResourcePackDownloadData(url, uuid, sha1)));
                            }
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().warn("[CustomApi] Get resource pack download link failed", ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (this.localFilePath != null) resourcePackPath = this.localFilePath;
        Path finalResourcePackPath = resourcePackPath;
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/api/v1/upload-resource-pack"))
                        .header("Authorization", authKey)
                        .header("Content-Type", "application/octet-stream")
                        .PUT(HttpRequest.BodyPublishers.ofFile(finalResourcePackPath))
                        .build();
                long uploadStart = System.currentTimeMillis();
                CraftEngine.instance().logger().info("[CustomApi] Starting file upload...");
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            long uploadTime = System.currentTimeMillis() - uploadStart;
                            CraftEngine.instance().logger().info(
                                    "[CustomApi] Upload request completed in " + uploadTime + "ms");
                            if (response.statusCode() == 200) {
                                future.complete(null);
                            } else {
                                future.completeExceptionally(new RuntimeException("Upload failed with status code: " + response.statusCode()));
                            }
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().warn("[CustomApi] Upload resource pack failed", ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            } catch (FileNotFoundException e) {
                CraftEngine.instance().logger().warn("[CustomApi] Resource pack not found: " + finalResourcePackPath);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private Map<String, Object> parseJson(String json) {
        try {
            return GsonHelper.get().fromJson(
                    json,
                    new TypeToken<Map<String, Object>>() {}.getType()
            );
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid JSON response: " + json, e);
        }
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            String apiUrl = (String) arguments.get("api-url");
            String authKey = (String) arguments.get("auth-key");
            if (apiUrl == null || apiUrl.isEmpty()) {
                throw new IllegalArgumentException("'api-url' cannot be empty for custom api host");
            }
            String localFilePath = (String) arguments.get("local-file-path");
            return new CustomApiHost(apiUrl, authKey, localFilePath);
        }
    }
}
