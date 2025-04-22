package net.momirealms.craftengine.core.plugin.script.argument;

import net.momirealms.craftengine.core.plugin.script.TokenStringReader;

public class IntArgumentParser implements ArgumentParser<Integer> {

    @Override
    public Integer parse(TokenStringReader reader) {
        String token = reader.nextToken();
        return Integer.parseInt(token);
    }
}
