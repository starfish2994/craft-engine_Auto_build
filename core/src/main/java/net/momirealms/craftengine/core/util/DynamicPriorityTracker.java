package net.momirealms.craftengine.core.util;

import net.momirealms.craftengine.core.plugin.config.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicPriorityTracker {

    public static class Element {
        private final int entityId;
        private volatile double distance;
        private final Object removePacket;

        public Element(int entityId, double distance, Object removePacket) {
            this.entityId = entityId;
            this.distance = distance;
            this.removePacket = removePacket;
        }

        public int entityId() {
            return entityId;
        }
        public double distance() {
            return distance;
        }
        public void setDistance(double distance) {
            this.distance = distance;
        }
        public Object removePacket() {
            return removePacket;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "entityId=" + entityId +
                    ", distance=" + distance +
                    ", removePacket=" + removePacket +
                    '}';
        }
    }

    public static class UpdateResult {
        private final List<Element> entered = new ArrayList<>();
        private final List<Element> exited = new ArrayList<>();

        public List<Element> getEntered() {
            return entered;
        }
        public List<Element> getExited() {
            return exited;
        }

        void addEntered(Element e) {
            entered.add(e);
        }
        void addExited(Element e) {
            exited.add(e);
        }

        @Override
        public String toString() {
            return "UpdateResult{" +
                    "entered=" + entered +
                    ", exited=" + exited +
                    '}';
        }
    }

    private Integer maxVisibleFurniture;
    private final PriorityQueue<Element> maxHeap;
    private final Map<Integer, Element> elementMap = new ConcurrentHashMap<>();
    private final Set<Integer> inHeapSet = ConcurrentHashMap.newKeySet();
    private final ReentrantLock heapLock = new ReentrantLock();

    public DynamicPriorityTracker() {
        this.maxVisibleFurniture = Config.maxVisibleFurniture();
        this.maxHeap = new PriorityQueue<>((a, b) -> Double.compare(b.distance, a.distance));
    }

    public void setMaxVisibleFurniture(int maxVisibleFurniture) {
        this.maxVisibleFurniture = maxVisibleFurniture;
    }

    public UpdateResult addOrUpdateElement(Element newElement) {
        UpdateResult result = new UpdateResult();
        heapLock.lock();
        try {
            Element existing = elementMap.get(newElement.entityId);

            if (existing != null) {
                return handleExistingElement(existing, newElement, result);
            } else {
                return handleNewElement(newElement, result);
            }
        } finally {
            heapLock.unlock();
        }
    }

    private UpdateResult handleNewElement(Element newElement, UpdateResult result) {
        elementMap.put(newElement.entityId, newElement);

        if (maxHeap.size() < maxVisibleFurniture) {
            maxHeap.offer(newElement);
            inHeapSet.add(newElement.entityId);
            result.addEntered(newElement);
        } else if (maxHeap.peek() != null && newElement.distance < maxHeap.peek().distance) {
            Element removed = maxHeap.poll();
            inHeapSet.remove(removed.entityId);
            result.addExited(removed);

            maxHeap.offer(newElement);
            inHeapSet.add(newElement.entityId);
            result.addEntered(newElement);
        }
        return result;
    }

    private UpdateResult handleExistingElement(Element existing, Element newElement, UpdateResult result) {
        existing.setDistance(newElement.distance);

        boolean wasInHeap = inHeapSet.contains(existing.entityId);
        boolean nowInHeap = checkIfShouldBeInHeap(existing.distance);

        if (wasInHeap) {
            maxHeap.remove(existing);
            maxHeap.offer(existing);
        } else if (nowInHeap) {
            if (maxHeap.size() < maxVisibleFurniture) {
                maxHeap.offer(existing);
                inHeapSet.add(existing.entityId);
                result.addEntered(existing);
            } else if (maxHeap.peek() != null && existing.distance < maxHeap.peek().distance) {
                Element removed = maxHeap.poll();
                inHeapSet.remove(removed.entityId);
                result.addExited(removed);

                maxHeap.offer(existing);
                inHeapSet.add(existing.entityId);
                result.addEntered(existing);
            }
        }
        return result;
    }

    private boolean checkIfShouldBeInHeap(double distance) {
        if (maxHeap.size() < maxVisibleFurniture) return true;
        return maxHeap.peek() != null && distance < maxHeap.peek().distance;
    }

    public int getTotalMembers() {
        heapLock.lock();
        try {
            return elementMap.size();
        } finally {
            heapLock.unlock();
        }
    }

    public List<Element> getAllElements() {
        heapLock.lock();
        try {
            return List.copyOf(elementMap.values());
        } finally {
            heapLock.unlock();
        }
    }

    public void removeByEntityId(int entityId) {
        heapLock.lock();
        try {
            Element removed = elementMap.remove(entityId);
            if (removed != null) {
                maxHeap.remove(removed);
                inHeapSet.remove(entityId);
            }
        } finally {
            heapLock.unlock();
        }
    }

    @Override
    public String toString() {
        return "DynamicPriorityTracker{" +
                "maxHeap=" + maxHeap +
                ", elementMap=" + elementMap +
                ", inHeapSet=" + inHeapSet +
                ", heapLock=" + heapLock +
                '}';
    }
}
