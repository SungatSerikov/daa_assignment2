public final class LinkedList {
    private static final class Node {
        private final int value;
        private Node next;

        private Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public int size() {
        return size;
    }

    public void add(int value) {
        Node added = new Node(value);
        if (tail == null) {
            head = added;
        } else {
            tail.next = added;
        }
        tail = added;
        size++;
    }

    public void add(int index, int value) {
        checkInsertionIndex(index);
        if (index == size) {
            add(value);
            return;
        }

        Node added = new Node(value);
        if (index == 0) {
            added.next = head;
            head = added;
        } else {
            Node previous = nodeAt(index - 1);
            added.next = previous.next;
            previous.next = added;
        }
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        if (index == 0) {
            int removed = head.value;
            head = head.next;
            size--;
            if (size == 0) {
                tail = null;
            }
            return removed;
        }

        Node previous = nodeAt(index - 1);
        Node removed = previous.next;
        previous.next = removed.next;
        if (removed == tail) {
            tail = previous;
        }
        size--;
        return removed.value;
    }

    public int get(int index) {
        return nodeAt(index).value;
    }

    public boolean contains(int value) {
        for (Node current = head; current != null; current = current.next) {
            if (current.value == value) {
                return true;
            }
        }
        return false;
    }

    private Node nodeAt(int index) {
        checkElementIndex(index);
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
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
