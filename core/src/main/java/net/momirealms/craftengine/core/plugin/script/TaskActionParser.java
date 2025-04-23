package net.momirealms.craftengine.core.plugin.script;

public interface TaskActionParser {

    <T> Action<T> parse(TokenStringReader reader);
}
