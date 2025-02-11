package net.momirealms.craftengine.core.block;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.registry.Holder;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockStateHolder {
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAP_PRINTER = entry -> {
        if (entry == null) {
            return "<NULL>";
        }
        Property<?> property = entry.getKey();
        return property.name() + "=" + formatValue(property, entry.getValue());
    };

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String formatValue(Property<T> property, Comparable<?> value) {
        return property.valueName((T) value);
    }

    protected final Holder<CustomBlock> owner;
    private final Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap;
    private Map<Property<?>, ImmutableBlockState[]> withMap;

    public BlockStateHolder(Holder<CustomBlock> owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap) {
        this.owner = owner;
        this.propertyMap = propertyMap;
    }

    public Holder<CustomBlock> owner() {
        return owner;
    }

    public <T extends Comparable<T>> ImmutableBlockState cycle(Property<T> property) {
        return this.with(property, getNextValue(property.possibleValues(), this.get(property)));
    }

    protected static <T> T getNextValue(List<T> values, T currentValue) {
        int nextIndex = (values.indexOf(currentValue) + 1) % values.size();
        return values.get(nextIndex);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.owner.value().id());
        if (!this.getEntries().isEmpty()) {
            result.append('[');
            result.append(this.getEntries().entrySet().stream()
                    .map(PROPERTY_MAP_PRINTER)
                    .collect(Collectors.joining(",")));
            result.append(']');
        }
        return result.toString();
    }

    public String getPropertiesAsString() {
        if (!this.getEntries().isEmpty()) {
            return this.getEntries().entrySet().stream()
                    .map(PROPERTY_MAP_PRINTER)
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.propertyMap.keySet());
    }

    public <T extends Comparable<T>> boolean contains(Property<T> property) {
        return this.propertyMap.containsKey(property);
    }

    public <T extends Comparable<T>> T get(Property<T> property) {
        Comparable<?> value = this.propertyMap.get(property);
        if (value == null) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.owner);
        }
        return property.valueClass().cast(value);
    }

    public <T extends Comparable<T>> Optional<T> getOrEmpty(Property<T> property) {
        return Optional.ofNullable(this.getNullable(property));
    }

    public <T extends Comparable<T>> T get(Property<T> property, T fallback) {
        return Objects.requireNonNullElse(this.getNullable(property), fallback);
    }

    @Nullable
    public <T extends Comparable<T>> T getNullable(Property<T> property) {
        Comparable<?> value = this.propertyMap.get(property);
        return value == null ? null : property.valueClass().cast(value);
    }

    public <T extends Comparable<T>, V extends T> ImmutableBlockState with(Property<T> property, V value) {
        Comparable<?> currentValue = this.propertyMap.get(property);
        if (currentValue == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.owner);
        }
        return this.withInternal(property, value, currentValue);
    }

    public <T extends Comparable<T>, V extends T> ImmutableBlockState withIfExists(Property<T> property, V value) {
        Comparable<?> currentValue = this.propertyMap.get(property);
        return currentValue == null ? ((ImmutableBlockState) this) : this.withInternal(property, value, currentValue);
    }

    private <T extends Comparable<T>, V extends T> ImmutableBlockState withInternal(Property<T> property, V newValue, Comparable<?> currentValue) {
        if (currentValue.equals(newValue)) {
            return (ImmutableBlockState) this;
        }
        int valueIndex = property.indexOf(newValue);
        if (valueIndex < 0) {
            throw new IllegalArgumentException("Cannot set property " + property + " to " + newValue + " on " + this.owner + ", it is not an allowed value");
        }
        return (this.withMap.get(property))[valueIndex];
    }

    public void createWithMap(Map<Map<Property<?>, Comparable<?>>, ImmutableBlockState> states) {
        if (this.withMap != null) {
            throw new IllegalStateException("withMap is already initialized.");
        }
        Map<Property<?>, ImmutableBlockState[]> map = new Reference2ObjectArrayMap<>(this.propertyMap.size());
        for (Map.Entry<Property<?>, Comparable<?>> entry : this.propertyMap.entrySet()) {
            Property<?> property = entry.getKey();
            ImmutableBlockState[] possibleStates = property.possibleValues().stream()
                    .map(value -> states.get(this.createPropertyMap(property, value)))
                    .toArray(ImmutableBlockState[]::new);
            map.put(property, possibleStates);
        }
        this.withMap = map;
    }

    private Map<Property<?>, Comparable<?>> createPropertyMap(Property<?> property, Comparable<?> value) {
        Map<Property<?>, Comparable<?>> newMap = new Reference2ObjectArrayMap<>(this.propertyMap);
        newMap.put(property, value);
        return newMap;
    }

    public Map<Property<?>, Comparable<?>> getEntries() {
        return this.propertyMap;
    }
}
