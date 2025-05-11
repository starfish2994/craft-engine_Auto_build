package net.momirealms.craftengine.bukkit.util;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlockTags {
    private static final Map<Key, Object> CACHE = new HashMap<>();

    private BlockTags() {}

    public static Object getOrCreate(Key key) {
        Object value = CACHE.get(key);
        if (value == null) {
            try {
                value = Reflections.method$TagKey$create.invoke(null, Reflections.instance$Registries$BLOCK, KeyUtils.toResourceLocation(key));
                CACHE.put(key, value);
                return value;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create block tag: " + key, e);
            }
        } else {
            return value;
        }
    }

    /**
     * 用于测试下面的 buildFakeUpdateTagsPacket 方法
     *
     * @param player CraftEngine玩家对象
     * @param reset 是否重置标签
     * @param targetBlock 测试添加标签的目标方块
     * @param setTag 测试添加的标签
     */
    public static void test(Player player, boolean reset, String targetBlock, String setTag) {
        Map<Object, Map<String, IntList>> addTags = new HashMap<>();
        if (!reset) {
            Object registries = Reflections.instance$BuiltInRegistries$BLOCK;
            Object key = FastNMS.INSTANCE.method$Registry$key(registries);
            Map<String, IntList> blockTags = new HashMap<>();
            IntList blockId = new IntArrayList();
            Object blockKey = KeyUtils.toResourceLocation(Key.of(targetBlock));
            Object block = FastNMS.INSTANCE.method$Registry$get(registries, blockKey);
            Optional<Integer> optionalBlockId = FastNMS.INSTANCE.method$BuiltInRegistries$getId(registries, block);
            optionalBlockId.ifPresent(integer -> blockId.add(integer.intValue()));
            blockTags.put(setTag, blockId);
            addTags.put(key, blockTags);
        }
        Object packet = buildFakeUpdateTagsPacket(addTags);
        player.sendPacket(packet, true);
    }

    /**
     * 构建模拟标签更新数据包（用于向客户端添加虚拟标签）
     *
     * @param addTags 需要添加的标签数据，结构为嵌套映射：
     *               <pre>{@code
     *               Map结构示例:
     *               {
     *                 注册表键1 (如BuiltInRegistries.ITEM.key) -> {
     *                   "命名空间:值1" -> IntList.of(1, 2, 3),  // 该命名空间下生效的物品ID列表
     *                   "命名空间:值2" -> IntList.of(5, 7)
     *                 },
     *                 注册表键2 (如BuiltInRegistries.BLOCK.key) -> {
     *                   "minecraft:beacon_base_blocks" -> IntList.of(1024, 2048)
     *                 },
     *                 ....
     *               }
     *               }</pre>
     *               其中：</br>
     *               - 外层键：注册表对象（如物品/方块注册表）</br>
     *               - 中间层键：标签的命名空间:值（字符串）</br>
     *               - 值：包含注册表内项目数字ID的IntList
     *
     * @return 可发送给客户端的 ClientboundUpdateTagsPacket 数据包对象
     */
    @SuppressWarnings("unchecked")
    public static Object buildFakeUpdateTagsPacket(Map<Object, Map<String, IntList>> addTags) {
        Map<Object, Object> registriesNetworkPayload = (Map<Object, Object>) FastNMS.INSTANCE.method$TagNetworkSerialization$serializeTagsToNetwork();
        for (Map.Entry<Object, Map<String, IntList>> entry : addTags.entrySet()) {
            Object registryKey = entry.getKey();
            Map<String, IntList> tagsToAdd = entry.getValue();
            Object existingPayload = registriesNetworkPayload.get(registryKey);
            if (existingPayload == null) continue;
            FriendlyByteBuf deserializeBuf = new FriendlyByteBuf(Unpooled.buffer());
            FastNMS.INSTANCE.method$TagNetworkSerialization$NetworkPayload$write(existingPayload, deserializeBuf);
            Map<String, IntList> combinedTags = deserializeBuf.readMap(
                    FriendlyByteBuf::readUtf,
                    FriendlyByteBuf::readIntIdList
            );
            combinedTags.putAll(tagsToAdd);
            FriendlyByteBuf serializeBuf = new FriendlyByteBuf(Unpooled.buffer());
            serializeBuf.writeMap(combinedTags,
                    FriendlyByteBuf::writeUtf,
                    FriendlyByteBuf::writeIntIdList
            );
            Object mergedPayload = FastNMS.INSTANCE.method$TagNetworkSerialization$NetworkPayload$read(serializeBuf);
            registriesNetworkPayload.put(registryKey, mergedPayload);
        }
        return FastNMS.INSTANCE.constructor$ClientboundUpdateTagsPacket(registriesNetworkPayload);
    }
}
