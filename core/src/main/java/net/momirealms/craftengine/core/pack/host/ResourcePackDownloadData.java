package net.momirealms.craftengine.core.pack.host;

import java.util.UUID;

public record ResourcePackDownloadData(String url, UUID uuid, String sha1) {
}
