package net.momirealms.craftengine.core.pack.host.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.Tuple;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OneDriveHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final String clientId;
    private final String clientSecret;
    private final ProxySelector proxy;
    private final String filePath;
    private final Path localFilePath;
    private Tuple<String, String, Date> refreshToken;
    private String sha1;
    private String fileId;

    public OneDriveHost(String clientId,
                        String clientSecret,
                        String refreshToken,
                        String filePath,
                        String localFilePath,
                        ProxySelector proxy) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.proxy = proxy;
        this.filePath = filePath;
        this.localFilePath = localFilePath == null ? null : Path.of(localFilePath);
        this.refreshToken = Tuple.of(refreshToken, "", new Date());
        readCacheFromDisk();
    }

    public void readCacheFromDisk() {
        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("onedrive.cache");
        if (!Files.exists(cachePath)) return;

        try (InputStream is = Files.newInputStream(cachePath)) {
            Map<String, String> cache = GsonHelper.get().fromJson(
                    new InputStreamReader(is),
                    new TypeToken<Map<String, String>>(){}.getType()
            );

            this.refreshToken = Tuple.of(
                    cache.get("refresh-token"),
                    cache.get("access-token"),
                    new Date(Long.parseLong(cache.get("refresh-token-expires-in"))));
            this.sha1 = cache.get("sha1");
            this.fileId = cache.get("file-id");

            CraftEngine.instance().logger().info("[OneDrive] Loaded cached resource pack info");
        } catch (Exception e) {
            CraftEngine.instance().logger().warn(
                    "[OneDrive] Failed to read cache file: " + e.getMessage());
        }
    }

    public void saveCacheToDisk() {
        Map<String, String> cache = new HashMap<>();
        cache.put("refresh-token", this.refreshToken.left());
        cache.put("access-token", this.refreshToken.mid());
        cache.put("refresh-token-expires-in", String.valueOf(this.refreshToken.right().getTime()));
        cache.put("sha1", this.sha1);
        cache.put("file-id", this.fileId);

        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("onedrive.cache");
        try {
            Files.writeString(
                    cachePath,
                    GsonHelper.get().toJson(cache),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            CraftEngine.instance().logger().warn(
                    "[OneDrive] Failed to save cache: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        CompletableFuture<List<ResourcePackDownloadData>> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                String accessToken = getOrRefreshJwtToken();
                saveCacheToDisk();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://graph.microsoft.com/v1.0/drive/items/" + fileId))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/octet-stream")
                        .GET()
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() != 200) {
                                CraftEngine.instance().logger().severe("[OneDrive] Failed to request resource pack download link: " + response.body());
                                future.completeExceptionally(new IOException("Failed to request resource pack download link: " + response.body()));
                                return;
                            }
                            String downloadUrl = parseJson(response.body()).get("@microsoft.graph.downloadUrl").getAsString();
                            future.complete(List.of(new ResourcePackDownloadData(
                                    downloadUrl,
                                    UUID.nameUUIDFromBytes(sha1.getBytes(StandardCharsets.UTF_8)),
                                    sha1
                            )));
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().severe("[OneDrive] Failed to request resource pack download link", ex);
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
            sha1 = calculateLocalFileSha1(finalResourcePackPath);
            String accessToken = getOrRefreshJwtToken();
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://graph.microsoft.com/v1.0/drive/root:/" + filePath + ":/content"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/octet-stream")
                        .PUT(HttpRequest.BodyPublishers.ofFile(finalResourcePackPath))
                        .build();
                long uploadStart = System.currentTimeMillis();
                CraftEngine.instance().logger().info("[OneDrive] Starting file upload...");
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                CraftEngine.instance().logger().info("[OneDrive] Uploaded resource pack in " + (System.currentTimeMillis() - uploadStart) + "ms");
                                fileId = parseJson(response.body()).get("id").getAsString();
                                saveCacheToDisk();
                                future.complete(null);
                            } else {
                                CraftEngine.instance().logger().warn("[OneDrive] Failed to upload resource pack: " + response.statusCode());
                                future.completeExceptionally(new RuntimeException("Failed to upload resource pack: " + response.statusCode()));
                            }
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().warn("[OneDrive] Failed to upload resource pack", ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            } catch (FileNotFoundException e) {
                CraftEngine.instance().logger().warn("[OneDrive] File not found: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private String getOrRefreshJwtToken() {
        if (refreshToken == null || refreshToken.mid().isEmpty() || refreshToken.right().before(new Date())) {
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                String formData = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                        "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                        "&redirect_uri=" + URLEncoder.encode("https://alist.nn.ci/tool/onedrive/callback", StandardCharsets.UTF_8) +
                        "&refresh_token=" + URLEncoder.encode(refreshToken.left(), StandardCharsets.UTF_8) +
                        "&grant_type=refresh_token" +
                        "&scope=Files.ReadWrite.All+offline_access";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://login.microsoftonline.com/common/oauth2/v2.0/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(formData))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    CraftEngine.instance().logger().warn("[OneDrive] Failed to refresh JWT token: " + response.body());
                    return refreshToken != null ? refreshToken.mid() : "";
                }

                JsonObject jsonData = parseJson(response.body());
                if (jsonData.has("error")) {
                    CraftEngine.instance().logger().warn("[OneDrive] Token refresh error: " + jsonData);
                    throw new RuntimeException("Token refresh failed: " + jsonData);
                }
                long expiresInMillis = jsonData.get("expires_in").getAsInt() * 1000L;
                refreshToken = Tuple.of(
                        jsonData.get("refresh_token").getAsString(),
                        jsonData.get("access_token").getAsString(),
                        new Date(System.currentTimeMillis() + expiresInMillis - 10_000)
                );
            } catch (IOException | InterruptedException e) {
                CraftEngine.instance().logger().warn("[OneDrive] Token refresh failed: " + e.getMessage());
                throw new RuntimeException("Token refresh failed", e);
            }
        }

        return refreshToken.mid();
    }

    private JsonObject parseJson(String json) {
        try {
            return GsonHelper.get().fromJson(
                    json,
                    JsonObject.class
            );
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid JSON response: " + json, e);
        }
    }

    private String calculateLocalFileSha1(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate SHA1", e);
        }
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            String clientId = (String) arguments.get("client-id");
            if (clientId == null || clientId.isEmpty()) {
                throw new RuntimeException("Missing 'client-id' for OneDriveHost");
            }
            String clientSecret = (String) arguments.get("client-secret");
            if (clientSecret == null || clientSecret.isEmpty()) {
                throw new RuntimeException("Missing 'client-secret' for OneDriveHost");
            }
            String refreshToken = (String) arguments.get("refresh-token");
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new RuntimeException("Missing 'refresh-token' for OneDriveHost");
            }
            String filePath = (String) arguments.getOrDefault("file-path", "resource_pack.zip");
            if (filePath == null || filePath.isEmpty()) {
                throw new RuntimeException("Missing 'file-path' for OneDriveHost");
            }
            String localFilePath = (String) arguments.get("local-file-path");
            ProxySelector proxy = MiscUtils.getProxySelector(arguments.get("proxy"));
            return new OneDriveHost(clientId, clientSecret, refreshToken, filePath, localFilePath, proxy);
        }
    }
}
