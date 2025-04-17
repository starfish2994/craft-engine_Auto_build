package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.MiscUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class S3Host implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner presigner;
    private final String bucket;
    private final String uploadPath;
    private final String uploadFileName;
    private final String cdnDomain;
    private final String cdnProtocol;
    private final Duration validity;

    public S3Host(
            S3AsyncClient s3AsyncClient,
            S3Presigner presigner,
            String bucket,
            String uploadPath,
            String uploadFileName,
            String cdnDomain,
            String cdnProtocol,
            Duration validity
    ) {
        this.s3AsyncClient = s3AsyncClient;
        this.presigner = presigner;
        this.bucket = bucket;
        this.uploadPath = uploadPath;
        this.uploadFileName = uploadFileName;
        this.cdnDomain = cdnDomain;
        this.cdnProtocol = cdnProtocol;
        this.validity = validity;
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        String objectKey = uploadPath + uploadFileName;

        return s3AsyncClient.headObject(HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .build())
                .thenApply(headResponse -> {
                    String sha1 = headResponse.metadata().get("sha1");
                    if (sha1 == null) {
                        throw new IllegalStateException("SHA1 metadata missing for object: " + objectKey);
                    }
                    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                            .signatureDuration(validity)
                            .getObjectRequest(b -> b.bucket(bucket).key(objectKey))
                            .build();
                    return Collections.singletonList(
                            ResourcePackDownloadData.of(
                                    replaceWithCdnUrl(presigner.presignGetObject(presignRequest).url()),
                                    UUID.nameUUIDFromBytes(sha1.getBytes(StandardCharsets.UTF_8)), sha1
                            )
                    );
                });
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        String objectKey = uploadPath + uploadFileName;
        String sha1 = calculateLocalFileSha1(resourcePackPath);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .metadata(Map.of("sha1", sha1))
                .build();
        return s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromFile(resourcePackPath))
                .thenApply(response -> {
                    CraftEngine.instance().logger().info("Uploaded resource pack to S3: " + objectKey);
                    return null;
                });
    }

    private String replaceWithCdnUrl(URL originalUrl) {
        if (cdnDomain == null) return originalUrl.toString();
        return cdnProtocol + "://" + cdnDomain
                + originalUrl.getPath()
                + (originalUrl.getQuery() != null ? "?" + originalUrl.getQuery() : "");
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
        @SuppressWarnings("deprecation")
        public ResourcePackHost create(Map<String, Object> arguments) {
            String endpoint = (String) arguments.get("endpoint");
            if (endpoint == null || endpoint.isEmpty()) {
                throw new IllegalArgumentException("'endpoint' cannot be empty for S3 host");
            }
            String protocol = (String) arguments.getOrDefault("protocol", "https");
            boolean usePathStyle = (boolean) arguments.getOrDefault("path-style", false);
            String bucket = (String) arguments.get("bucket");
            if (bucket == null || bucket.isEmpty()) {
                throw new IllegalArgumentException("'bucket' cannot be empty for S3 host");
            }
            String region = (String) arguments.getOrDefault("region", "auto");
            String accessKeyId = (String) arguments.get("access-key-id");
            if (accessKeyId == null || accessKeyId.isEmpty()) {
                throw new IllegalArgumentException("'access-key-id' cannot be empty for S3 host");
            }
            String accessKeySecret = (String) arguments.get("access-key-secret");
            if (accessKeySecret == null || accessKeySecret.isEmpty()) {
                throw new IllegalArgumentException("'access-key-secret' cannot be empty for S3 host");
            }
            String uploadPath = (String) arguments.getOrDefault("upload-path", "");
            String uploadFileName = (String) arguments.getOrDefault("upload-file-name", "resource_pack.zip");
            boolean useLegacySignature = (boolean) arguments.getOrDefault("use-legacy-signature", true);
            Duration validity = Duration.ofSeconds((int) arguments.getOrDefault("validity", 10));

            Map<String, Object> cdn = MiscUtils.castToMap(arguments.get("cdn"), true);
            String cdnDomain = null;
            String cdnProtocol = "https";
            if (cdn != null) {
                cdnDomain = (String) cdn.get("domain");
                cdnProtocol = (String) cdn.getOrDefault("protocol", "https");
            }

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, accessKeySecret);

            S3AsyncClientBuilder s3AsyncClientBuilder = S3AsyncClient.builder()
                    .endpointOverride(URI.create(protocol + "://" + endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .serviceConfiguration(b -> b.pathStyleAccessEnabled(usePathStyle));

            if (useLegacySignature) {
                s3AsyncClientBuilder.overrideConfiguration(b -> b
                        .putAdvancedOption(SdkAdvancedClientOption.SIGNER, AwsS3V4Signer.create())
                );
            }

            S3AsyncClient s3AsyncClient = s3AsyncClientBuilder.build();

            S3Presigner presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(protocol + "://" + endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            return new S3Host(
                    s3AsyncClient,
                    presigner,
                    bucket,
                    uploadPath,
                    uploadFileName,
                    cdnDomain,
                    cdnProtocol,
                    validity
            );
        }
    }
}
