package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.util.TriConsumer;
import net.momirealms.sparrow.nbt.ByteTag;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface NetworkItemHandler<T> {
    Operation[] BY_INDEX = new Operation[] {Operation.ADD, Operation.REMOVE, Operation.RESET};
    String NETWORK_ITEM_TAG = "craftengine:network";
    String NETWORK_OPERATION = "type";
    String NETWORK_VALUE = "value";

    Optional<Item<T>> s2c(Item<T> itemStack, ItemBuildContext context);

    Optional<Item<T>> c2s(Item<T> itemStack, ItemBuildContext context);

    static CompoundTag pack(Operation operation, @Nullable Tag value) {
        if (value == null) {
            return new CompoundTag(Map.of(NETWORK_OPERATION, operation.tag()));
        } else {
            return new CompoundTag(Map.of(NETWORK_OPERATION, operation.tag(), NETWORK_VALUE, value));
        }
    }

    static CompoundTag pack(Operation operation) {
        return new CompoundTag(Map.of(NETWORK_OPERATION, operation.tag()));
    }

    static <T> void apply(String componentType, CompoundTag networkData, Item<T> item) {
        byte index = networkData.getByte(NETWORK_OPERATION);
        Operation operation = BY_INDEX[index];
        operation.consumer.accept(item, componentType, operation == Operation.ADD ? networkData.get(NETWORK_VALUE) : null);
    }

    enum Operation {
        ADD(0, Item::setNBTComponent),
        REMOVE(1, (i, s, t) -> i.removeComponent(s)),
        RESET(2, (i, s, t) -> i.resetComponent(s));

        private final int id;
        private final ByteTag tag;
        private final TriConsumer<Item<?>, String, Tag> consumer;

        Operation(int id, TriConsumer<Item<?>, String, Tag> consumer) {
            this.id = id;
            this.tag = new ByteTag((byte) id);
            this.consumer = consumer;
        }

        public int id() {
            return this.id;
        }

        public ByteTag tag() {
            return tag;
        }
    }
}
