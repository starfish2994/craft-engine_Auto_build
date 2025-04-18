package net.momirealms.craftengine.core.pack.host.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.HashUtils;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AlistHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final String apiUrl;
    private final String userName;
    private final String password;
    private final String filePassword;
    private final String otpCode;
    private final Duration jwtTokenExpiration;
    private final String filePath;
    private final boolean disabledUpload;
    private final ProxySelector proxy;
    private Pair<String, Date> jwtToken;
    private String cacheSha1;

    public AlistHost(String apiUrl,
                     String userName,
                     String password,
                     String filePassword,
                     String otpCode,
                     Duration jwtTokenExpiration,
                     String filePath,
                     boolean disabledUpload,
                     ProxySelector proxy) {
        this.apiUrl = apiUrl;
        this.userName = userName;
        this.password = password;
        this.filePassword = filePassword;
        this.otpCode = otpCode;
        this.jwtTokenExpiration = jwtTokenExpiration;
        this.filePath = filePath;
        this.disabledUpload = disabledUpload;
        this.proxy = proxy;
        this.readCacheFromDisk();
    }

    private void readCacheFromDisk() {
        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("alist.cache");
        if (!Files.exists(cachePath)) return;
        try (InputStream is = Files.newInputStream(cachePath)) {
            cacheSha1 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("[Alist] Failed to read cache file", e);
        }
    }

    private void saveCacheToDisk() {
        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("alist.cache");
        try {
            Files.writeString(cachePath, cacheSha1);
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("[Alist] Failed to write cache file", e);
        }
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        CompletableFuture<List<ResourcePackDownloadData>> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/api/fs/get"))
                        .header("Authorization", getOrRefreshJwtToken())
                        .header("Content-Type", "application/json")
                        .POST(getRequestResourcePackDownloadLinkPost())
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> handleResourcePackDownloadLinkResponse(response, future))
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().severe("[Alist] Failed to request resource pack download link", ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        if (disabledUpload) {
            cacheSha1 = "";
            saveCacheToDisk();
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/api/fs/put"))
                        .header("Authorization", getOrRefreshJwtToken())
                        .header("File-Path", URLEncoder.encode(filePath, StandardCharsets.UTF_8)
                                .replace("/", "%2F"))
                        .header("overwrite", "true")
                        .header("password", filePassword)
                        .header("Content-Type", "application/x-zip-compressed")
                        .PUT(HttpRequest.BodyPublishers.ofFile(resourcePackPath))
                        .build();
                long requestStart = System.currentTimeMillis();
                CraftEngine.instance().logger().info("[Alist] Starting file upload...");
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            long uploadTime = System.currentTimeMillis() - requestStart;
                            if (response.statusCode() == 200) {
                                cacheSha1 = HashUtils.calculateLocalFileSha1(resourcePackPath);
                                saveCacheToDisk();
                                CraftEngine.instance().logger().info("[Alist] Upload resource pack successfully in " + uploadTime + "ms");
                                future.complete(null);
                            } else {
                                future.completeExceptionally(new RuntimeException("Upload failed with status code: " + response.statusCode()));
                            }
                        })
                        .exceptionally(ex -> {
                            long uploadTime = System.currentTimeMillis() - requestStart;
                            CraftEngine.instance().logger().severe(
                                    "[Alist] Failed to upload resource pack after " + uploadTime + "ms", ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            } catch (IOException e) {
                CraftEngine.instance().logger().warn("[Alist] Failed to upload resource pack: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Nullable
    private String getOrRefreshJwtToken() {
        if (jwtToken == null || jwtToken.right().before(new Date())) {
            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(getLoginPost())
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    CraftEngine.instance().logger().warn("[Alist] Failed to get JWT token: " + response.body());
                    return null;
                }
                JsonObject jsonData = GsonHelper.parseJsonToJsonObject(response.body());
                JsonElement code = jsonData.get("code");
                if (code.isJsonPrimitive() && code.getAsJsonPrimitive().isNumber() && code.getAsJsonPrimitive().getAsInt() == 200) {
                    JsonElement data = jsonData.get("data");
                    if (data.isJsonObject()) {
                        JsonObject jsonObj = data.getAsJsonObject();
                        jwtToken = Pair.of(
                                jsonObj.getAsJsonPrimitive("token").getAsString(),
                                new Date(System.currentTimeMillis() + jwtTokenExpiration.toMillis())
                        );
                        return jwtToken.left();
                    }
                    CraftEngine.instance().logger().warn("[Alist] Failed to get JWT token: " + response.body());
                    return null;
                }
                CraftEngine.instance().logger().warn("[Alist] Failed to get JWT token: " + response.body());
                return null;
            } catch (IOException | InterruptedException e) {
                CraftEngine.instance().logger().warn("[Alist] Failed to get JWT token", e);
                return null;
            }
        }
        return jwtToken.left();
    }

    private HttpRequest.BodyPublisher getLoginPost() {
        String body = "{\"username\":\"" + userName + "\",\"password\":\"" + password + "\"";
        if (otpCode != null && !otpCode.isEmpty()) {
            body += ",\"otp_code\":\"" + otpCode + "\"";
        }
        body += "}";
        return HttpRequest.BodyPublishers.ofString(body);
    }

    private HttpRequest.BodyPublisher getRequestResourcePackDownloadLinkPost() {
        String body = "{\"path\":\"" + filePath + "\",\"password\":\"" + filePassword + "\"}";
        return HttpRequest.BodyPublishers.ofString(body);
    }

    private void handleResourcePackDownloadLinkResponse(
            HttpResponse<String> response, CompletableFuture<List<ResourcePackDownloadData>> future) {
        if (response.statusCode() == 200) {
            JsonObject json = GsonHelper.parseJsonToJsonObject(response.body());
            JsonElement code = json.get("code");
            if (code.isJsonPrimitive() && code.getAsJsonPrimitive().isNumber() && code.getAsJsonPrimitive().getAsInt() == 200) {
                JsonElement data = json.get("data");
                if (data.isJsonObject()) {
                    JsonObject dataObj = data.getAsJsonObject();
                    boolean isDir = dataObj.getAsJsonPrimitive("is_dir").getAsBoolean();
                    if (!isDir) {
                        String url = dataObj.getAsJsonPrimitive("raw_url").getAsString();
                        if ((cacheSha1 == null || cacheSha1.isEmpty()) && disabledUpload) {
                            try (HttpClient client = HttpClient.newBuilder().proxy(proxy).build()) {
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .GET()
                                        .build();
                                HttpResponse<InputStream> responseHash = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                                try (InputStream inputStream = responseHash.body()) {
                                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                                    byte[] buffer = new byte[8192];
                                    int len;
                                    while ((len = inputStream.read(buffer)) != -1) {
                                        md.update(buffer, 0, len);
                                    }
                                    byte[] digest = md.digest();
                                    cacheSha1 = HexFormat.of().formatHex(digest);
                                    saveCacheToDisk();
                                } catch (NoSuchAlgorithmException e) {
                                    future.completeExceptionally(new RuntimeException("Failed to get resource pack hash"));
                                    return;
                                }
                            } catch (IOException | InterruptedException e) {
                                future.completeExceptionally(new RuntimeException("Failed to get resource pack hash"));
                                return;
                            }
                        }
                        UUID uuid = UUID.nameUUIDFromBytes(Objects.requireNonNull(cacheSha1).getBytes(StandardCharsets.UTF_8));
                        future.complete(List.of(new ResourcePackDownloadData(url, uuid, cacheSha1)));
                        return;
                    }
                }
            }
        }
        future.completeExceptionally(
                new RuntimeException("Failed to request resource pack download link: " + response.body()));
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        public ResourcePackHost create(Map<String, Object> arguments) {
            String apiUrl = (String) arguments.get("api-url");
            if (apiUrl == null || apiUrl.isEmpty()) {
                throw new IllegalArgumentException("'api-url' cannot be empty for Alist host");
            }
            String userName = (String) arguments.get("username");
            if (userName == null || userName.isEmpty()) {
                throw new IllegalArgumentException("'username' cannot be empty for Alist host");
            }
            String password = (String) arguments.get("password");
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("'password' cannot be empty for Alist host");
            }
            String filePassword = (String) arguments.getOrDefault("file-password", "");
            String otpCode = (String) arguments.get("otp-code");
            Duration jwtTokenExpiration = Duration.ofHours((int) arguments.getOrDefault("jwt-token-expiration", 48));
            String filePath = (String) arguments.get("file-path");
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("'file-path' cannot be empty for Alist host");
            }
            boolean disabledUpload = (boolean) arguments.getOrDefault("disabled-upload", false);
            ProxySelector proxy = MiscUtils.getProxySelector(arguments.get("proxy"));
            return new AlistHost(apiUrl, userName, password, filePassword, otpCode, jwtTokenExpiration, filePath, disabledUpload, proxy);
        }
    }
}
