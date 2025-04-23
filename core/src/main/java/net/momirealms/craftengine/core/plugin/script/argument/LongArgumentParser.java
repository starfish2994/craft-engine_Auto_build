package net.momirealms.craftengine.core.plugin.script.argument;

import net.momirealms.craftengine.core.plugin.script.TokenStringReader;

public class LongArgumentParser implements ArgumentParser<Long> {

    @Override
    public Long parse(TokenStringReader reader) {
        String token = reader.nextToken();
        return Long.parseLong(token);
    }
}
