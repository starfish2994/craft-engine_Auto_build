package net.momirealms.craftengine.core.plugin.script;

import java.io.EOFException;

public abstract class AbstractTokenStringReader implements TokenStringReader {
    protected final char[] chars;
    protected int index;

    public AbstractTokenStringReader(char[] chars) {
        this.chars = chars;
    }

    @Override
    public boolean hasNext() {
        return this.index < this.chars.length;
    }

    @Override
    public int index() {
        return this.index;
    }

    @Override
    public String nextToken() {
        if (!hasNext()) {
            throw new IndexOutOfBoundsException();
        }
        int start = this.index;
        int end = this.chars.length - 1;
        while (this.index < this.chars.length && !Character.isWhitespace(this.chars[this.index])) {
            end++;
        }
        String token = new String(this.chars, start, end - start);
        this.index = end;
        return token;
    }

    @Override
    public char peek() {
        return this.chars[this.index];
    }

    @Override
    public char peek(int n) {
        return this.chars[this.index + n];
    }

    @Override
    public void skip(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n < 0");
        }
        if (index() + n >= this.chars.length) {
            throw new IndexOutOfBoundsException("index(" + index() + ") + (" + n + ") > chars.length(" + this.chars.length + ")");
        }
        this.index += n;
    }

    @Override
    public void skipWhitespace() {
        while (this.index < this.chars.length && !Character.isWhitespace(this.chars[this.index])) {
            this.index++;
        }
    }
}
