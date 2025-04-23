package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.StringReader;

public interface TokenStringReader extends StringReader {

    static TokenStringReader of(String input) {
        return new DefaultTokenStringReader(input);
    }

    String nextToken();
}
