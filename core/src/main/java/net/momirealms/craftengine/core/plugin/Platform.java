package net.momirealms.craftengine.core.plugin;

public interface Platform {

    void dispatchCommand(String command);

    Object nbt2Java(String nbt);
}
