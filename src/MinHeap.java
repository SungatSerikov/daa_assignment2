import java.util.NoSuchElementException;

public final class MinHeap {
    private int[] elements;
    private int size;

    public MinHeap() {
        this(4);
    }

    public MinHeap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        elements = new int[initialCapacity];
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return elements.length;
    }

    public void insert(int value) {
        ensureCapacity(size + 1);
        elements[size] = value;
        int index = size;
        size++;

        // Move the new value up until its parent is no larger than it.
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (elements[parent] <= elements[index]) {
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }

    public int peekMin() {
        if (size == 0) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elements[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new NoSuchElementException("Heap is empty");
        }

        int minimum = elements[0];
        size--;
        if (size == 0) {
            elements[0] = 0;
            return minimum;
        }

        elements[0] = elements[size];
        elements[size] = 0;
        siftDown(0);
        return minimum;
    }

    public boolean hasHeapProperty() {
        for (int child = 1; child < size; child++) {
            int parent = (child - 1) / 2;
            if (elements[parent] > elements[child]) {
                return false;
            }
        }
        return true;
    }

    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            if (left >= size) {
                return;
            }

            int right = left + 1;
            int smallerChild = left;
            if (right < size && elements[right] < elements[left]) {
                smallerChild = right;
            }
            if (elements[index] <= elements[smallerChild]) {
                return;
            }

            swap(index, smallerChild);
            index = smallerChild;
        }
    }

    private void swap(int first, int second) {
        int temporary = elements[first];
        elements[first] = elements[second];
        elements[second] = temporary;
    }

    private void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity <= elements.length) {
            return;
        }

        int doubled = elements.length == 0 ? 1 : elements.length * 2;
        int newCapacity = Math.max(requiredCapacity, doubled);
        int[] larger = new int[newCapacity];
        for (int i = 0; i < size; i++) {
            larger[i] = elements[i];
        }
        elements = larger;
    }
}
