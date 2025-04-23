package net.momirealms.craftengine.core.plugin.script.argument;

import net.momirealms.craftengine.core.plugin.script.TokenStringReader;

public class DoubleArgumentParser implements ArgumentParser<Double> {

    @Override
    public Double parse(TokenStringReader reader) {
        String token = reader.nextToken();
        return Double.parseDouble(token);
    }
}
