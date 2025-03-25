package net.momirealms.craftengine.bukkit.block.worldedit;

import java.util.Set;
import java.util.function.Predicate;

public class SuggestionHandler {
    private final Predicate<String> matcher;

    private SuggestionHandler(Predicate<String> matcher) {
        this.matcher = matcher;
    }

    public boolean matches(String input) {
        return matcher.test(input);
    }

    public static SuggestionHandler of(Integer... pos) {
        Set<Integer> valid = Set.of(pos);
        return new SuggestionHandler(input -> {
            if (input.contains("  ")) return false;

            String[] args = input.split(" ");
            int index = input.endsWith(" ") ? args.length : args.length - 1;
            return valid.contains(index);
        });
    }

    public static SuggestionHandler custom(Predicate<String> matcher) {
        return new SuggestionHandler(matcher);
    }
}
