public final class DynamicArray {
    private int[] elements;
    private int size;

    public DynamicArray() {
        this(4);
    }

    public DynamicArray(int initialCapacity) {
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

    public void add(int value) {
        ensureCapacity(size + 1);
        elements[size] = value;
        size++;
    }

    public void add(int index, int value) {
        checkInsertionIndex(index);
        ensureCapacity(size + 1);

        // Move from right to left so no value is overwritten before it is copied.
        for (int i = size; i > index; i--) {
            elements[i] = elements[i - 1];
        }
        elements[index] = value;
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        int removed = elements[index];

        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }
        size--;
        elements[size] = 0;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        return elements[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            if (elements[i] == value) {
                return true;
            }
        }
        return false;
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

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkInsertionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}
