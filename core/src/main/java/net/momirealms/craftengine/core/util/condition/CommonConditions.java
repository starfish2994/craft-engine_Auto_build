package net.momirealms.craftengine.core.util.condition;

import net.momirealms.craftengine.core.util.Key;

public final class CommonConditions {
    private CommonConditions() {}

    public static final Key ALL_OF = Key.of("craftengine:all_of");
    public static final Key ANY_OF = Key.of("craftengine:any_of");
    public static final Key INVERTED = Key.of("craftengine:inverted");
}
