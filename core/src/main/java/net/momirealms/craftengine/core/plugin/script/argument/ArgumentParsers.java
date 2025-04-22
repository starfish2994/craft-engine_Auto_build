package net.momirealms.craftengine.core.plugin.script.argument;

public class ArgumentParsers {
    public static final ArgumentParser<Integer> INT_PARSER = new IntArgumentParser();
    public static final ArgumentParser<Long> LONG_PARSER = new LongArgumentParser();
    public static final ArgumentParser<Double> DOUBLE_PARSER = new DoubleArgumentParser();

    public static ArgumentParser<Integer> intParser() {
        return INT_PARSER;
    }

    public static ArgumentParser<Long> longParser() {
        return LONG_PARSER;
    }

    public static ArgumentParser<Double> doubleParser() {
        return DOUBLE_PARSER;
    }
}
