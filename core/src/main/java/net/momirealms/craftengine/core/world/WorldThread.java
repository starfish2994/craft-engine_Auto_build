package net.momirealms.craftengine.core.world;

public class WorldThread extends Thread {

    private final CEWorld world;

    public WorldThread(Runnable target, String name, CEWorld world) {
        super(target, name);
        this.world = world;
    }

    public static boolean isWorldThreadFor(CEWorld world) {
        if (Thread.currentThread() instanceof WorldThread worldThread) {
            return worldThread.world == world;
        }
        return false;
    }
}
