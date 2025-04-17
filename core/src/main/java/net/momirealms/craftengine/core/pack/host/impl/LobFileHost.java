package net.momirealms.craftengine.core.pack.host.impl;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LobFileHost implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final Path forcedPackPath;
    private final String apiKey;

    private String url;
    private String sha1;
    private UUID uuid;

    public LobFileHost(String localFile, String apiKey) {
        this.forcedPackPath = localFile == null ? null : ResourcePackHost.customPackPath(localFile);
        this.apiKey = apiKey;
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        if (url == null) return CompletableFuture.completedFuture(Collections.emptyList());
        return CompletableFuture.completedFuture(List.of(ResourcePackDownloadData.of(url, uuid, sha1)));
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (this.forcedPackPath != null) resourcePackPath = forcedPackPath;
        Path finalResourcePackPath = resourcePackPath;
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try {
                Map<String, String> hashes = calculateHashes(finalResourcePackPath);
                String sha1Hash = hashes.get("SHA-1");
                String sha256Hash = hashes.get("SHA-256");

                try (HttpClient client = HttpClient.newHttpClient()) {
                    String boundary = UUID.randomUUID().toString();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://lobfile.com/api/v3/upload.php"))
                            .header("X-API-Key", apiKey)
                            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                            .POST(buildMultipartBody(finalResourcePackPath, sha256Hash, boundary))
                            .build();

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenAccept(response -> handleUploadResponse(response, future, sha1Hash))
                            .exceptionally(ex -> {
                                future.completeExceptionally(ex);
                                return null;
                            });
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private Map<String, String> calculateHashes(Path path) throws IOException, NoSuchAlgorithmException {
        Map<String, String> hashes = new HashMap<>();
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, sha1Digest)) {
            DigestInputStream dis2 = new DigestInputStream(dis, sha256Digest);

            while (dis2.read() != -1) ;

            hashes.put("SHA-1", bytesToHex(sha1Digest.digest()));
            hashes.put("SHA-256", bytesToHex(sha256Digest.digest()));
        }
        return hashes;
    }

    private HttpRequest.BodyPublisher buildMultipartBody(Path filePath, String sha256Hash, String boundary) throws IOException {
        List<byte[]> parts = new ArrayList<>();
        String filePartHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.getFileName() + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";
        parts.add(filePartHeader.getBytes());

        parts.add(Files.readAllBytes(filePath));
        parts.add("\r\n".getBytes());

        String sha256Part = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"sha_256\"\r\n\r\n" +
                sha256Hash + "\r\n";
        parts.add(sha256Part.getBytes());

        String endBoundary = "--" + boundary + "--\r\n";
        parts.add(endBoundary.getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(parts);
    }

    private void handleUploadResponse(
            HttpResponse<String> response,
            CompletableFuture<Void> future,
            String localSha1
    ) {
        try {
            if (response.statusCode() == 200) {
                Map<String, Object> json = parseJson(response.body());

                if (Boolean.TRUE.equals(json.get("success"))) {
                    this.url = (String) json.get("url");
                    this.sha1 = localSha1;
                    this.uuid = UUID.randomUUID();
                    CraftEngine.instance().logger().info("[LobFile] Upload success! Resource pack URL: " + this.url);
                    future.complete(null);
                } else {
                    future.completeExceptionally(new RuntimeException((String) json.get("error")));
                }
            } else {
                future.completeExceptionally(new RuntimeException("Upload failed: " + response.statusCode()));
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
            String localFilePath = (String) arguments.get("local-file-path");
            String apiKey = (String) arguments.get("api-key");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("Missing 'api-key' for LobFileHost");
            }
            return new LobFileHost(localFilePath, apiKey);
        }
    }
}