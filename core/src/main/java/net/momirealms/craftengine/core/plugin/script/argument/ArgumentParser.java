package net.momirealms.craftengine.core.plugin.script.argument;

import net.momirealms.craftengine.core.plugin.script.TokenStringReader;

public interface ArgumentParser<T> {

    T parse(TokenStringReader reader);
}
