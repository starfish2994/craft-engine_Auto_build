package net.momirealms.craftengine.core.plugin.script;

public interface TokenStringReader {

    char peek();

    char peek(int n);

    void skip(int n);

    int index();

    boolean hasNext();

    String nextToken();

    void skipWhitespace();
}
