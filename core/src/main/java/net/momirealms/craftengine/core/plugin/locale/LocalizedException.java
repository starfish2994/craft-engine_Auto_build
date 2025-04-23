package net.momirealms.craftengine.core.plugin.locale;

public class LocalizedException extends RuntimeException {
    private final String node;
    private final String[] arguments;

    public LocalizedException(String node, String... arguments) {
        super(node);
        this.node = node;
        this.arguments = arguments;
    }

    public String[] arguments() {
        return arguments;
    }

    public String node() {
        return node;
    }
}
