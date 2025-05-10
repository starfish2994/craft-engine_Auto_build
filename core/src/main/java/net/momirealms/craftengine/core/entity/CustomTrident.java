package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.util.Key;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record CustomTrident(Key customTridentItemId, Byte displayType, Vector3f translation, Quaternionf rotationLefts) {
}
