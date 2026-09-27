import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

public class Tests {
    private static int checks = 0;

    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        System.out.println("All tests passed: " + checks + " checks");
    }

    private static void testDynamicArray() {
        DynamicArray array = new DynamicArray(0);
        equal(0, array.size(), "new array size");
        check(!array.contains(10), "empty array contains");

        array.add(10);
        equal(1, array.capacity(), "growth from zero capacity");
        array.add(20);
        array.add(30);
        equal(4, array.capacity(), "capacity doubles");

        array.add(0, 5);
        array.add(2, 15);
        array.add(array.size(), 40);
        equal(5, array.get(0), "insert at start");
        equal(15, array.get(2), "insert in middle");
        equal(40, array.get(5), "insert at end");

        equal(5, array.remove(0), "remove first");
        equal(15, array.remove(1), "remove middle");
        equal(40, array.remove(array.size() - 1), "remove last");
        equal(3, array.size(), "size after removals");
        equal(10, array.get(0), "first value after removals");
        equal(20, array.get(1), "middle value after removals");
        equal(30, array.get(2), "last value after removals");

        array.add(20);
        array.remove(1);
        check(array.contains(20), "duplicate remains");
        array.remove(2);
        check(!array.contains(20), "both duplicates removed");

        try {
            array.get(-1);
            throw new AssertionError("get(-1) should fail");
        } catch (IndexOutOfBoundsException expected) {
            checks++;
        }
        try {
            array.add(array.size() + 1, 7);
            throw new AssertionError("insert past end should fail");
        } catch (IndexOutOfBoundsException expected) {
            checks++;
        }
        try {
            array.remove(array.size());
            throw new AssertionError("remove at size should fail");
        } catch (IndexOutOfBoundsException expected) {
            checks++;
        }

        DynamicArray one = new DynamicArray();
        one.add(7);
        equal(7, one.remove(0), "remove only element");
        equal(0, one.size(), "empty after removing only element");

        compareArrayWithJava();

        DynamicArray large = new DynamicArray();
        for (int i = 0; i < 100_000; i++) {
            large.add(i);
        }
        equal(100_000, large.size(), "large array size");
        equal(0, large.get(0), "large array first");
        equal(50_000, large.get(50_000), "large array middle");
        equal(99_999, large.get(99_999), "large array last");
    }

    private static void compareArrayWithJava() {
        DynamicArray mine = new DynamicArray();
        ArrayList<Integer> javaList = new ArrayList<>();
        Random random = new Random(42);

        for (int step = 0; step < 1_000; step++) {
            int value = random.nextInt(41) - 20;
            int operation = random.nextInt(5);
            if (operation == 0) {
                mine.add(value);
                javaList.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(javaList.size() + 1);
                mine.add(index, value);
                javaList.add(index, value);
            } else if (operation == 2 && !javaList.isEmpty()) {
                int index = random.nextInt(javaList.size());
                equal(javaList.remove(index), mine.remove(index), "random array remove");
            } else if (operation == 3 && !javaList.isEmpty()) {
                int index = random.nextInt(javaList.size());
                equal(javaList.get(index), mine.get(index), "random array get");
            } else if (operation == 4) {
                check(javaList.contains(value) == mine.contains(value), "random array contains");
            }
            equal(javaList.size(), mine.size(), "random array size");
        }
        for (int i = 0; i < javaList.size(); i++) {
            equal(javaList.get(i), mine.get(i), "final array contents");
        }
    }

    private static void testLinkedList() {
        LinkedList list = new LinkedList();
        equal(0, list.size(), "new list size");
        check(!list.contains(10), "empty list contains");

        list.add(10);
        list.add(20);
        list.add(30);
        list.add(0, 5);
        list.add(2, 15);
        list.add(list.size(), 40);
        equal(5, list.get(0), "list insert at start");
        equal(15, list.get(2), "list insert in middle");
        equal(40, list.get(5), "list insert at end");

        equal(5, list.remove(0), "list remove first");
        equal(15, list.remove(1), "list remove middle");
        equal(40, list.remove(list.size() - 1), "list remove last");
        equal(3, list.size(), "list size after removals");
        equal(10, list.get(0), "list first after removals");
        equal(20, list.get(1), "list middle after removals");
        equal(30, list.get(2), "list last after removals");

        list.add(20);
        list.remove(1);
        check(list.contains(20), "list duplicate remains");
        list.remove(2);
        check(!list.contains(20), "list duplicates removed");

        try {
            list.get(-1);
            throw new AssertionError("list get(-1) should fail");
        } catch (IndexOutOfBoundsException expected) {
            checks++;
        }
        try {
            list.add(list.size() + 1, 7);
            throw new AssertionError("list insert past end should fail");
        } catch (IndexOutOfBoundsException expected) {
            checks++;
        }
        try {
            list.remove(list.size());
            throw new AssertionError("list remove at size should fail");
        } catch (IndexOutOfBoundsException expected) {
            checks++;
        }

        LinkedList one = new LinkedList();
        one.add(7);
        equal(7, one.remove(0), "list remove only element");
        one.add(8);
        one.add(9);
        equal(9, one.remove(1), "list remove tail");
        one.add(10);
        equal(10, one.get(1), "list append after tail removal");

        compareListWithJava();

        LinkedList large = new LinkedList();
        for (int i = 0; i < 100_000; i++) {
            large.add(i);
        }
        equal(100_000, large.size(), "large list size");
        equal(0, large.get(0), "large list first");
        equal(50_000, large.get(50_000), "large list middle");
        equal(99_999, large.get(99_999), "large list last");
    }

    private static void compareListWithJava() {
        LinkedList mine = new LinkedList();
        java.util.LinkedList<Integer> javaList = new java.util.LinkedList<>();
        Random random = new Random(42);

        for (int step = 0; step < 1_000; step++) {
            int value = random.nextInt(41) - 20;
            int operation = random.nextInt(5);
            if (operation == 0) {
                mine.add(value);
                javaList.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(javaList.size() + 1);
                mine.add(index, value);
                javaList.add(index, value);
            } else if (operation == 2 && !javaList.isEmpty()) {
                int index = random.nextInt(javaList.size());
                equal(javaList.remove(index), mine.remove(index), "random list remove");
            } else if (operation == 3 && !javaList.isEmpty()) {
                int index = random.nextInt(javaList.size());
                equal(javaList.get(index), mine.get(index), "random list get");
            } else if (operation == 4) {
                check(javaList.contains(value) == mine.contains(value), "random list contains");
            }
            equal(javaList.size(), mine.size(), "random list size");
        }
        for (int i = 0; i < javaList.size(); i++) {
            equal(javaList.get(i), mine.get(i), "final list contents");
        }
    }

    private static void testMinHeap() {
        MinHeap heap = new MinHeap(0);
        equal(0, heap.size(), "new heap size");
        try {
            heap.peekMin();
            throw new AssertionError("peek on empty heap should fail");
        } catch (java.util.NoSuchElementException expected) {
            checks++;
        }
        try {
            heap.extractMin();
            throw new AssertionError("extract from empty heap should fail");
        } catch (java.util.NoSuchElementException expected) {
            checks++;
        }

        heap.insert(7);
        equal(1, heap.capacity(), "heap growth from zero capacity");
        equal(7, heap.peekMin(), "heap singleton peek");
        equal(7, heap.extractMin(), "heap singleton extract");
        equal(0, heap.size(), "heap empty again");

        int[] input = {3, 5, 8, 12, 7, 2, 2};
        for (int value : input) {
            heap.insert(value);
            check(heap.hasHeapProperty(), "heap property after insert");
        }
        int[] sorted = {2, 2, 3, 5, 7, 8, 12};
        for (int value : sorted) {
            equal(value, heap.extractMin(), "heap extraction order");
            check(heap.hasHeapProperty(), "heap property after extract");
        }

        compareHeapWithJava();

        MinHeap large = new MinHeap();
        Random random = new Random(123);
        for (int i = 0; i < 100_000; i++) {
            large.insert(random.nextInt());
        }
        equal(100_000, large.size(), "large heap size");
        check(large.hasHeapProperty(), "large heap property");
        int previous = Integer.MIN_VALUE;
        while (large.size() > 0) {
            int current = large.extractMin();
            check(previous <= current, "large heap output sorted");
            previous = current;
        }
    }

    private static void compareHeapWithJava() {
        MinHeap mine = new MinHeap();
        PriorityQueue<Integer> javaHeap = new PriorityQueue<>();
        Random random = new Random(42);

        for (int step = 0; step < 2_000; step++) {
            int operation = random.nextInt(3);
            if (javaHeap.isEmpty() || operation == 0) {
                int value = random.nextInt(41) - 20;
                mine.insert(value);
                javaHeap.add(value);
            } else if (operation == 1) {
                equal(javaHeap.element(), mine.peekMin(), "random heap peek");
            } else {
                equal(javaHeap.remove(), mine.extractMin(), "random heap extract");
            }
            equal(javaHeap.size(), mine.size(), "random heap size");
            check(mine.hasHeapProperty(), "random heap property");
        }
    }

    private static void equal(int expected, int actual, String message) {
        check(expected == actual, message + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
