package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.CustomTridentUtils;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("forceUpdate", BooleanParser.booleanParser())
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().itemManager().cachedSuggestions());
                    }
                }))
                .required("interpolationDelay", IntegerParser.integerParser())
                .required("transformationInterpolationDuration", IntegerParser.integerParser())
                .required("positionRotationInterpolationDuration", IntegerParser.integerParser())
                // .required("displayType", ByteParser.byteParser())
                // .required("x", FloatParser.floatParser())
                // .required("y", FloatParser.floatParser())
                // .required("z", FloatParser.floatParser())
                // .required("w", FloatParser.floatParser())
                .handler(context -> {
                    Player player = context.sender();
                    if (context.get("forceUpdate")) {
                        net.momirealms.craftengine.core.entity.player.Player cePlayer = plugin().adapt(player);
                        Collection<Trident> tridents = player.getWorld().getEntitiesByClass(Trident.class);
                        List<Object> packets = new ArrayList<>();
                        for (Trident trident : tridents) {
                            int entityId = FastNMS.INSTANCE.method$Entity$getId(FastNMS.INSTANCE.method$CraftEntity$getHandle(trident));
                            player.sendMessage("COMMAND entityId: " + entityId);
                            packets.add(CustomTridentUtils.buildCustomTridentSetEntityDataPacket(cePlayer, entityId));
                        }
                        cePlayer.sendPackets(packets, true);
                        return;
                    }
                    NamespacedKey namespacedKey = context.get("id");
                    ItemStack item = new ItemStack(Material.TRIDENT);
                    // NamespacedKey displayTypeKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:display_type"));
                    // NamespacedKey customTridentX = Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident_x"));
                    // NamespacedKey customTridentY = Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident_y"));
                    // NamespacedKey customTridentZ = Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident_z"));
                    // NamespacedKey customTridentW = Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident_w"));
                    item.editMeta(meta -> {
                        meta.getPersistentDataContainer().set(CustomTridentUtils.customTridentKey, PersistentDataType.STRING, namespacedKey.asString());
                        meta.getPersistentDataContainer().set(CustomTridentUtils.interpolationDelayKey, PersistentDataType.INTEGER, context.get("interpolationDelay"));
                        meta.getPersistentDataContainer().set(CustomTridentUtils.transformationInterpolationDurationaKey, PersistentDataType.INTEGER, context.get("transformationInterpolationDuration"));
                        meta.getPersistentDataContainer().set(CustomTridentUtils.positionRotationInterpolationDurationKey, PersistentDataType.INTEGER, context.get("positionRotationInterpolationDuration"));
                        // container.set(displayTypeKey, PersistentDataType.BYTE, context.get("displayType"));
                        // container.set(customTridentX, PersistentDataType.FLOAT, context.get("x"));
                        // container.set(customTridentY, PersistentDataType.FLOAT, context.get("y"));
                        // container.set(customTridentZ, PersistentDataType.FLOAT, context.get("z"));
                        // container.set(customTridentW, PersistentDataType.FLOAT, context.get("w"));
                        Item<ItemStack> ceItem = BukkitItemManager.instance().createWrappedItem(Key.of(namespacedKey.asString()), null);
                        Optional<Integer> customModelData = ceItem.customModelData();
                        customModelData.ifPresent(meta::setCustomModelData);
                    });
                    player.getInventory().addItem(item);
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
