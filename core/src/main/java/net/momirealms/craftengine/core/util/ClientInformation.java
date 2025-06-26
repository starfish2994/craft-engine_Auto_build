package net.momirealms.craftengine.core.util;

public record ClientInformation(String language, int viewDistance, Object chatVisibility,
                                boolean chatColors, int modelCustomisation, Object mainHand,
                                boolean textFilteringEnabled, boolean allowsListing, Object particleStatus) {
}
