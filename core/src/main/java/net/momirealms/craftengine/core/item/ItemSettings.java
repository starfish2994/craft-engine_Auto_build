package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.projectile.ProjectileMeta;
import net.momirealms.craftengine.core.entity.projectile.ProjectileType;
import net.momirealms.craftengine.core.item.modifier.EquippableModifier;
import net.momirealms.craftengine.core.item.modifier.FoodModifier;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.item.setting.*;
import net.momirealms.craftengine.core.pack.misc.EquipmentLayerType;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class ItemSettings {
    int fuelTime;
    Set<Key> tags = Set.of();
    @Nullable
    ItemEquipment equipment;
    boolean canRepair = true;
    List<AnvilRepairItem> anvilRepairItems = List.of();
    boolean renameable = true;
    boolean canPlaceRelatedVanillaBlock = false;
    ProjectileMeta projectileMeta;
    boolean dyeable = true;
    Helmet helmet = null;
    FoodData foodData = null;
    Key consumeReplacement = null;
    Key craftRemainder = null;
    List<DamageSource> invulnerable = List.of();
    boolean canEnchant = true;
    float compostProbability= 0.5f;

    private ItemSettings() {}

    public <I> List<ItemDataModifier<I>> modifiers() {
        ArrayList<ItemDataModifier<I>> modifiers = new ArrayList<>();
        if (VersionHelper.isOrAbove1_21_2() && this.equipment != null) {
            modifiers.add(new EquippableModifier<>(this.equipment.data()));
        }
        if (VersionHelper.isOrAbove1_20_5() && this.foodData != null) {
            modifiers.add(new FoodModifier<>(this.foodData.nutrition(), this.foodData.saturation(), false));
        }
        return modifiers;
    }

    public static ItemSettings of() {
        return new ItemSettings();
    }

    public static ItemSettings fromMap(Map<String, Object> map) {
        if (map == null) return ItemSettings.of();
        return applyModifiers(ItemSettings.of(), map);
    }

    public static ItemSettings ofFullCopy(ItemSettings settings) {
        ItemSettings newSettings = of();
        newSettings.fuelTime = settings.fuelTime;
        newSettings.tags = settings.tags;
        newSettings.equipment = settings.equipment;
        newSettings.canRepair = settings.canRepair;
        newSettings.anvilRepairItems = settings.anvilRepairItems;
        newSettings.renameable = settings.renameable;
        newSettings.canPlaceRelatedVanillaBlock = settings.canPlaceRelatedVanillaBlock;
        newSettings.projectileMeta = settings.projectileMeta;
        newSettings.dyeable = settings.dyeable;
        newSettings.helmet = settings.helmet;
        newSettings.foodData = settings.foodData;
        newSettings.consumeReplacement = settings.consumeReplacement;
        newSettings.craftRemainder = settings.craftRemainder;
        newSettings.invulnerable = settings.invulnerable;
        newSettings.canEnchant = settings.canEnchant;
        newSettings.compostProbability = settings.compostProbability;
        return newSettings;
    }

    public static ItemSettings applyModifiers(ItemSettings settings, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ItemSettings.Modifier.Factory factory = ItemSettings.Modifiers.FACTORIES.get(entry.getKey());
            if (factory != null) {
                factory.createModifier(entry.getValue()).apply(settings);
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.settings.unknown", entry.getKey());
            }
        }
        return settings;
    }

    public ProjectileMeta projectileMeta() {
        return projectileMeta;
    }

    public boolean canPlaceRelatedVanillaBlock() {
        return canPlaceRelatedVanillaBlock;
    }

    public boolean canRepair() {
        return canRepair;
    }

    public int fuelTime() {
        return fuelTime;
    }

    public boolean renameable() {
        return renameable;
    }

    public Set<Key> tags() {
        return tags;
    }

    public boolean dyeable() {
        return dyeable;
    }

    public boolean canEnchant() {
        return canEnchant;
    }

    public List<AnvilRepairItem> repairItems() {
        return anvilRepairItems;
    }

    @Nullable
    public FoodData foodData() {
        return foodData;
    }

    @Nullable
    public Key consumeReplacement() {
        return consumeReplacement;
    }

    @Nullable
    public Key craftRemainder() {
        return craftRemainder;
    }

    @Nullable
    public Helmet helmet() {
        return helmet;
    }

    @Nullable
    public ItemEquipment equipment() {
        return equipment;
    }

    public List<DamageSource> invulnerable() {
        return invulnerable;
    }

    public float compostProbability() {
        return compostProbability;
    }

    public ItemSettings repairItems(List<AnvilRepairItem> items) {
        this.anvilRepairItems = items;
        return this;
    }

    public ItemSettings consumeReplacement(Key key) {
        this.consumeReplacement = key;
        return this;
    }

    public ItemSettings craftRemainder(Key key) {
        this.craftRemainder = key;
        return this;
    }

    public ItemSettings compostProbability(float chance) {
        this.compostProbability = chance;
        return this;
    }

    public ItemSettings canRepair(boolean canRepair) {
        this.canRepair = canRepair;
        return this;
    }

    public ItemSettings canEnchant(boolean canEnchant) {
        this.canEnchant = canEnchant;
        return this;
    }

    public ItemSettings renameable(boolean renameable) {
        this.renameable = renameable;
        return this;
    }

    public ItemSettings projectileMeta(ProjectileMeta projectileMeta) {
        this.projectileMeta = projectileMeta;
        return this;
    }

    public ItemSettings canPlaceRelatedVanillaBlock(boolean canPlaceRelatedVanillaBlock) {
        this.canPlaceRelatedVanillaBlock = canPlaceRelatedVanillaBlock;
        return this;
    }

    public ItemSettings fuelTime(int fuelTime) {
        this.fuelTime = fuelTime;
        return this;
    }

    public ItemSettings tags(Set<Key> tags) {
        this.tags = tags;
        return this;
    }

    public ItemSettings foodData(FoodData foodData) {
        this.foodData = foodData;
        return this;
    }

    public ItemSettings equipment(ItemEquipment equipment) {
        this.equipment = equipment;
        return this;
    }

    public ItemSettings dyeable(boolean bool) {
        this.dyeable = bool;
        return this;
    }

    public ItemSettings helmet(Helmet helmet) {
        this.helmet = helmet;
        return this;
    }

    public ItemSettings invulnerable(List<DamageSource> invulnerable) {
        this.invulnerable = invulnerable;
        return this;
    }

    @FunctionalInterface
    public interface Modifier {

        void apply(ItemSettings settings);

        @FunctionalInterface
        interface Factory {

            ItemSettings.Modifier createModifier(Object value);
        }
    }

    public static class Modifiers {
        private static final Map<String, ItemSettings.Modifier.Factory> FACTORIES = new HashMap<>();

        static {
            registerFactory("repairable", (value -> {
                boolean bool = ResourceConfigUtils.getAsBoolean(value, "repairable");
                return settings -> settings.canRepair(bool);
            }));
            registerFactory("enchantable", (value -> {
                boolean bool = ResourceConfigUtils.getAsBoolean(value, "enchantable");
                return settings -> settings.canEnchant(bool);
            }));
            registerFactory("renameable", (value -> {
                boolean bool = ResourceConfigUtils.getAsBoolean(value, "renameable");
                return settings -> settings.renameable(bool);
            }));
            registerFactory("anvil-repair-item", (value -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> materials = (List<Map<String, Object>>) value;
                List<AnvilRepairItem> anvilRepairItemList = new ArrayList<>();
                for (Map<String, Object> material : materials) {
                    int amount = ResourceConfigUtils.getAsInt(material.getOrDefault("amount", 0), "amount");
                    double percent = ResourceConfigUtils.getAsDouble(material.getOrDefault("percent", 0), "percent");
                    anvilRepairItemList.add(new AnvilRepairItem(MiscUtils.getAsStringList(material.get("target")), amount, percent));
                }
                return settings -> settings.repairItems(anvilRepairItemList);
            }));
            registerFactory("fuel-time", (value -> {
                int intValue = ResourceConfigUtils.getAsInt(value, "fuel-time");
                return settings -> settings.fuelTime(intValue);
            }));
            registerFactory("consume-replacement", (value -> settings -> {
                if (value == null) settings.consumeReplacement(null);
                else settings.consumeReplacement(Key.of(value.toString()));
            }));
            registerFactory("craft-remaining-item", (value -> settings -> {
                if (value == null) settings.craftRemainder(null);
                else settings.craftRemainder(Key.of(value.toString()));
            }));
            registerFactory("tags", (value -> {
                List<String> tags = MiscUtils.getAsStringList(value);
                return settings -> settings.tags(tags.stream().map(Key::of).collect(Collectors.toSet()));
            }));
            registerFactory("equippable", (value -> {
                Map<String, Object> args = MiscUtils.castToMap(value, false);
                EquipmentData data = EquipmentData.fromMap(args);
                ItemEquipment itemEquipment = new ItemEquipment(data);
                for (Map.Entry<String, Object> entry : args.entrySet()) {
                    EquipmentLayerType layerType = EquipmentLayerType.byId(entry.getKey());
                    if (layerType != null) {
                        itemEquipment.addLayer(layerType, ItemEquipment.Layer.fromConfig(entry.getValue()));
                    }
                }
                return settings -> settings.equipment(itemEquipment);
            }));
            registerFactory("can-place", (value -> {
                boolean bool = ResourceConfigUtils.getAsBoolean(value, "can-place");
                return settings -> settings.canPlaceRelatedVanillaBlock(bool);
            }));
            registerFactory("projectile", (value -> {
                Map<String, Object> args = MiscUtils.castToMap(value, false);
                Key customTridentItemId = Key.of(Objects.requireNonNull(args.get("item"), "'item should not be null'").toString());
                ItemDisplayContext displayType = ItemDisplayContext.valueOf(args.getOrDefault("display-transform", "NONE").toString().toUpperCase(Locale.ENGLISH));
                Billboard billboard = Billboard.valueOf(args.getOrDefault("billboard", "FIXED").toString().toUpperCase(Locale.ENGLISH));
                Vector3f translation = MiscUtils.getAsVector3f(args.getOrDefault("translation", "0"), "translation");
                Vector3f scale = MiscUtils.getAsVector3f(args.getOrDefault("scale", "1"), "scale");
                Quaternionf rotation = MiscUtils.getAsQuaternionf(ResourceConfigUtils.get(args, "rotation-left", "rotation"), "rotation-left");
                ProjectileType type = Optional.ofNullable(args.get("type")).map(String::valueOf).map(it -> ProjectileType.valueOf(it.toUpperCase(Locale.ENGLISH))).orElse(null);
                double range = ResourceConfigUtils.getAsDouble(args.getOrDefault("range", 1), "range");
                return settings -> settings.projectileMeta(new ProjectileMeta(customTridentItemId, displayType, billboard, scale, translation, rotation, range, type));
            }));
            registerFactory("helmet", (value -> {
                Map<String, Object> args = MiscUtils.castToMap(value, false);
                return settings -> settings.helmet(new Helmet(SoundData.create(args.getOrDefault("equip-sound", "minecraft:intentionally_empty"), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_1)));
            }));
            registerFactory("compost-probability", (value -> {
                float chance = ResourceConfigUtils.getAsFloat(value, "compost-probability");
                return settings -> settings.compostProbability(chance);
            }));
            registerFactory("dyeable", (value -> {
                boolean bool = ResourceConfigUtils.getAsBoolean(value, "dyeable");
                return settings -> settings.dyeable(bool);
            }));
            registerFactory("food", (value -> {
                Map<String, Object> args = MiscUtils.castToMap(value, false);
                FoodData data = new FoodData(
                        ResourceConfigUtils.getAsInt(args.get("nutrition"), "nutrition"),
                        ResourceConfigUtils.getAsFloat(args.get("saturation"), "saturation")
                );
                return settings -> settings.foodData(data);
            }));
            registerFactory("invulnerable", (value -> {
                List<DamageSource> list = MiscUtils.getAsStringList(value).stream().map(it -> {
                    DamageSource source = DamageSource.byName(it);
                    if (source == null) {
                        throw new LocalizedResourceConfigException("warning.config.item.settings.invulnerable.invalid_damage_source", it, EnumUtils.toString(DamageSource.values()));
                    }
                    return source;
                }).toList();
                return settings -> settings.invulnerable(list);
            }));
        }

        private static void registerFactory(String id, ItemSettings.Modifier.Factory factory) {
            FACTORIES.put(id, factory);
        }
    }
}
