package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.pack.host.ResourcePackHosts;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.HashUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class S3Host implements ResourcePackHost {
    public static final Factory FACTORY = new Factory();
    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner preSigner;
    private final String bucket;
    private final String uploadPath;
    private final String cdnDomain;
    private final String cdnProtocol;
    private final Duration validity;

    public S3Host(
            S3AsyncClient s3AsyncClient,
            S3Presigner preSigner,
            String bucket,
            String uploadPath,
            String cdnDomain,
            String cdnProtocol,
            Duration validity
    ) {
        this.s3AsyncClient = s3AsyncClient;
        this.preSigner = preSigner;
        this.bucket = bucket;
        this.uploadPath = uploadPath;
        this.cdnDomain = cdnDomain;
        this.cdnProtocol = cdnProtocol;
        this.validity = validity;
    }

    @Override
    public boolean canUpload() {
        return true;
    }

    @Override
    public Key type() {
        return ResourcePackHosts.S3;
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        return this.s3AsyncClient.headObject(HeadObjectRequest.builder()
                        .bucket(this.bucket)
                        .key(this.uploadPath)
                        .build())
                .handle((headResponse, exception) -> {
                    if (exception != null) {
                        Throwable cause = exception.getCause();
                        if (cause instanceof NoSuchKeyException) {
                            CraftEngine.instance().logger().warn("[S3] Resource pack not found in bucket '" + this.bucket + "'. Path: " + this.uploadPath);
                            return Collections.emptyList();
                        } else {
                            CraftEngine.instance().logger().warn(
                                    "[S3] Failed to retrieve resource pack metadata. Reason: " +
                                            cause.getClass().getSimpleName() + " - " + cause.getMessage()
                            );
                            throw new CompletionException("Metadata request failed for path: " + this.uploadPath, cause);
                        }
                    }
                    String sha1 = headResponse.metadata().get("sha1");
                    if (sha1 == null) {
                        CraftEngine.instance().logger().warn("[S3] Missing SHA-1 checksum in object metadata. Path: " + this.uploadPath);
                        throw new CompletionException(new IllegalStateException("Missing SHA-1 metadata for S3 object: " + this.uploadPath));
                    }
                    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                            .signatureDuration(this.validity)
                            .getObjectRequest(b -> b.bucket(this.bucket).key(this.uploadPath))
                            .build();
                    return Collections.singletonList(
                            ResourcePackDownloadData.of(
                                    replaceWithCdnUrl(this.preSigner.presignGetObject(presignRequest).url()),
                                    UUID.nameUUIDFromBytes(sha1.getBytes(StandardCharsets.UTF_8)),
                                    sha1
                            )
                    );
                });
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        String sha1 = HashUtils.calculateLocalFileSha1(resourcePackPath);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(this.bucket)
                .key(this.uploadPath)
                .metadata(Map.of("sha1", sha1))
                .build();
        long uploadStart = System.currentTimeMillis();
        CraftEngine.instance().logger().info("[S3] Initiating resource pack upload to '" + this.uploadPath + "'");
        return this.s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromFile(resourcePackPath))
                .handle((response, exception) -> {
                    if (exception != null) {
                        Throwable cause = exception instanceof CompletionException ?
                                exception.getCause() :
                                exception;
                        CraftEngine.instance().logger().warn(
                                "[S3] Upload failed for path '" + this.uploadPath + "'. Error: " +
                                        cause.getClass().getSimpleName() + " - " + cause.getMessage()
                        );
                        throw new CompletionException("Failed to upload to S3 path: " + this.uploadPath, cause);
                    }
                    CraftEngine.instance().logger().info(
                            "[S3] Successfully uploaded resource pack to '" + this.uploadPath + "' in " +
                                    (System.currentTimeMillis() - uploadStart) + " ms"
                    );
                    return null;
                });
    }

    private String replaceWithCdnUrl(URL originalUrl) {
        if (this.cdnDomain == null) return originalUrl.toString();
        return this.cdnProtocol + "://" + this.cdnDomain
                + originalUrl.getPath()
                + (originalUrl.getQuery() != null ? "?" + originalUrl.getQuery() : "");
    }

    public static class Factory implements ResourcePackHostFactory {

        @Override
        @SuppressWarnings("deprecation")
        public ResourcePackHost create(Map<String, Object> arguments) {
            boolean useEnv = (boolean) arguments.getOrDefault("use-environment-variables", false);
            String endpoint = (String) arguments.get("endpoint");
            if (endpoint == null || endpoint.isEmpty()) {
                throw new LocalizedException("warning.config.host.s3.missing_endpoint");
            }
            String protocol = (String) arguments.getOrDefault("protocol", "https");
            boolean usePathStyle = (boolean) arguments.getOrDefault("path-style", false);
            String bucket = (String) arguments.get("bucket");
            if (bucket == null || bucket.isEmpty()) {
                throw new LocalizedException("warning.config.host.s3.missing_bucket");
            }
            String region = (String) arguments.getOrDefault("region", "auto");
            String accessKeyId = useEnv ? System.getenv("CE_S3_ACCESS_KEY_ID") : (String) arguments.get("access-key-id");
            if (accessKeyId == null || accessKeyId.isEmpty()) {
                throw new LocalizedException("warning.config.host.s3.missing_access_key");
            }
            String accessKeySecret = useEnv ? System.getenv("CE_S3_ACCESS_KEY_SECRET") : (String) arguments.get("access-key-secret");
            if (accessKeySecret == null || accessKeySecret.isEmpty()) {
                throw new LocalizedException("warning.config.host.s3.missing_secret");
            }
            String uploadPath = (String) arguments.getOrDefault("upload-path", "craftengine/resource_pack.zip");
            if (uploadPath == null || uploadPath.isEmpty()) {
                throw new LocalizedException("warning.config.host.s3.missing_upload_path");
            }
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

            Map<String, Object> proxySetting = MiscUtils.castToMap(arguments.get("proxy"), true);
            if (proxySetting != null) {
                String host = (String) proxySetting.get("host");
                int port = (Integer) proxySetting.get("port");
                String scheme = (String) proxySetting.get("scheme");
                String username = (String) proxySetting.get("username");
                String password = (String) proxySetting.get("password");
                if (host == null || host.isEmpty() || port <= 0 || port > 65535 || scheme == null || scheme.isEmpty()) {
                    throw new IllegalArgumentException("Invalid proxy configuration");
                }
                ProxyConfiguration.Builder builder = ProxyConfiguration.builder().host(host).port(port).scheme(scheme);
                if (username != null) builder.username(username);
                if (password != null) builder.password(password);
                SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder().proxyConfiguration(builder.build()).build();
                s3AsyncClientBuilder.httpClient(httpClient);
            }

            S3AsyncClient s3AsyncClient = s3AsyncClientBuilder.build();

            S3Presigner preSigner = S3Presigner.builder()
                    .endpointOverride(URI.create(protocol + "://" + endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            return new S3Host(s3AsyncClient, preSigner, bucket, uploadPath, cdnDomain, cdnProtocol, validity);
        }
    }
}