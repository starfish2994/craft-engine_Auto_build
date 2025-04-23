package net.momirealms.craftengine.core.plugin.script;

import net.momirealms.craftengine.core.util.DefaultStringReader;

public class DefaultTokenStringReader extends DefaultStringReader implements TokenStringReader {

    public DefaultTokenStringReader(String string) {
        super(string);
    }

    @Override
    public String nextToken() {
        skipWhitespaceAndComment();
        return readString();
    }
}
