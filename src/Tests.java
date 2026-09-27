import java.util.ArrayList;
import java.util.Random;

public final class Tests {
    private static int checks;

    public static void main(String[] args) {
        testDynamicArrayExamples();
        testDynamicArrayBoundaries();
        testDynamicArrayAgainstArrayList();
        testDynamicArrayLargeInput();
        int dynamicArrayChecks = checks;
        System.out.println("DynamicArray tests passed: " + dynamicArrayChecks + " checks");

        testLinkedListExamples();
        testLinkedListBoundaries();
        testLinkedListAgainstJdkList();
        testLinkedListLargeInput();
        System.out.println("LinkedList tests passed: " + (checks - dynamicArrayChecks) + " checks");
    }

    private static void testDynamicArrayExamples() {
        DynamicArray values = new DynamicArray(2);
        checkEquals(0, values.size(), "empty size");
        checkEquals(2, values.capacity(), "initial capacity");

        values.add(10);
        values.add(20);
        values.add(30);
        checkEquals(4, values.capacity(), "capacity doubles when full");
        checkContents(values, 10, 20, 30);

        values.add(0, 5);
        values.add(2, 15);
        values.add(values.size(), 40);
        checkContents(values, 5, 10, 15, 20, 30, 40);

        checkEquals(5, values.remove(0), "remove first");
        checkEquals(15, values.remove(1), "remove middle");
        checkEquals(40, values.remove(values.size() - 1), "remove last");
        checkContents(values, 10, 20, 30);

        values.add(20);
        check(values.contains(20), "contains duplicate");
        check(!values.contains(99), "missing value");
        values.remove(1);
        check(values.contains(20), "one duplicate remains");
        values.remove(values.size() - 1);
        check(!values.contains(20), "all duplicates removed");
    }

    private static void testDynamicArrayBoundaries() {
        DynamicArray values = new DynamicArray(0);
        expectIndexOutOfBounds(() -> values.get(0), "get from empty array");
        expectIndexOutOfBounds(() -> values.remove(0), "remove from empty array");
        expectIndexOutOfBounds(() -> values.add(-1, 7), "negative insertion index");
        expectIndexOutOfBounds(() -> values.add(1, 7), "insertion past end");

        values.add(7);
        checkEquals(1, values.capacity(), "zero-capacity array grows");
        checkContents(values, 7);
        expectIndexOutOfBounds(() -> values.get(-1), "negative get index");
        expectIndexOutOfBounds(() -> values.get(values.size()), "get at size");
        expectIndexOutOfBounds(() -> values.remove(-1), "negative removal index");
        expectIndexOutOfBounds(() -> values.remove(values.size()), "remove at size");
        expectIndexOutOfBounds(() -> values.add(values.size() + 1, 8), "insertion past size");

        checkEquals(7, values.remove(0), "remove only element");
        checkEquals(0, values.size(), "empty after removal");
        check(!values.contains(7), "removed value not found");
    }

    private static void testDynamicArrayAgainstArrayList() {
        DynamicArray actual = new DynamicArray();
        ArrayList<Integer> expected = new ArrayList<>();
        Random random = new Random(42);

        for (int step = 0; step < 2_000; step++) {
            int value = random.nextInt(101) - 50;
            switch (random.nextInt(5)) {
                case 0 -> {
                    actual.add(value);
                    expected.add(value);
                }
                case 1 -> {
                    int index = random.nextInt(expected.size() + 1);
                    actual.add(index, value);
                    expected.add(index, value);
                }
                case 2 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        checkEquals(expected.remove(index), actual.remove(index), "random remove");
                    }
                }
                case 3 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        checkEquals(expected.get(index), actual.get(index), "random get");
                    }
                }
                case 4 -> check(expected.contains(value) == actual.contains(value), "random contains");
                default -> throw new AssertionError("Unexpected operation");
            }
            checkEquals(expected.size(), actual.size(), "random size");
        }

        for (int i = 0; i < expected.size(); i++) {
            checkEquals(expected.get(i), actual.get(i), "final content at " + i);
        }
    }

    private static void testDynamicArrayLargeInput() {
        DynamicArray values = new DynamicArray(0);
        for (int i = 0; i < 100_000; i++) {
            values.add(i);
        }
        checkEquals(100_000, values.size(), "large size");
        checkEquals(0, values.get(0), "large first value");
        checkEquals(50_000, values.get(50_000), "large middle value");
        checkEquals(99_999, values.get(99_999), "large last value");
    }

    private static void testLinkedListExamples() {
        LinkedList values = new LinkedList();
        checkEquals(0, values.size(), "empty linked list size");

        values.add(10);
        values.add(20);
        values.add(30);
        checkContents(values, 10, 20, 30);

        values.add(0, 5);
        values.add(2, 15);
        values.add(values.size(), 40);
        checkContents(values, 5, 10, 15, 20, 30, 40);

        checkEquals(5, values.remove(0), "linked remove first");
        checkEquals(15, values.remove(1), "linked remove middle");
        checkEquals(40, values.remove(values.size() - 1), "linked remove last");
        checkContents(values, 10, 20, 30);

        values.add(20);
        check(values.contains(20), "linked contains duplicate");
        check(!values.contains(99), "linked missing value");
        values.remove(1);
        check(values.contains(20), "linked duplicate remains");
        values.remove(values.size() - 1);
        check(!values.contains(20), "linked duplicates removed");
        values.add(35);
        checkContents(values, 10, 30, 35);
    }

    private static void testLinkedListBoundaries() {
        LinkedList values = new LinkedList();
        expectIndexOutOfBounds(() -> values.get(0), "linked get from empty list");
        expectIndexOutOfBounds(() -> values.remove(0), "linked remove from empty list");
        expectIndexOutOfBounds(() -> values.add(-1, 7), "linked negative insertion index");
        expectIndexOutOfBounds(() -> values.add(1, 7), "linked insertion past end");

        values.add(0, 7);
        checkContents(values, 7);
        expectIndexOutOfBounds(() -> values.get(-1), "linked negative get index");
        expectIndexOutOfBounds(() -> values.get(values.size()), "linked get at size");
        expectIndexOutOfBounds(() -> values.remove(-1), "linked negative removal index");
        expectIndexOutOfBounds(() -> values.remove(values.size()), "linked remove at size");
        expectIndexOutOfBounds(() -> values.add(values.size() + 1, 8), "linked insertion past size");

        checkEquals(7, values.remove(0), "linked remove only element");
        checkEquals(0, values.size(), "linked empty after removal");
        check(!values.contains(7), "linked removed value not found");
        values.add(8);
        values.add(9);
        checkContents(values, 8, 9);
        checkEquals(9, values.remove(1), "linked remove tail");
        values.add(10);
        checkContents(values, 8, 10);
    }

    private static void testLinkedListAgainstJdkList() {
        LinkedList actual = new LinkedList();
        java.util.LinkedList<Integer> expected = new java.util.LinkedList<>();
        Random random = new Random(42);

        for (int step = 0; step < 2_000; step++) {
            int value = random.nextInt(101) - 50;
            switch (random.nextInt(5)) {
                case 0 -> {
                    actual.add(value);
                    expected.add(value);
                }
                case 1 -> {
                    int index = random.nextInt(expected.size() + 1);
                    actual.add(index, value);
                    expected.add(index, value);
                }
                case 2 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        checkEquals(expected.remove(index), actual.remove(index), "linked random remove");
                    }
                }
                case 3 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        checkEquals(expected.get(index), actual.get(index), "linked random get");
                    }
                }
                case 4 -> check(expected.contains(value) == actual.contains(value), "linked random contains");
                default -> throw new AssertionError("Unexpected operation");
            }
            checkEquals(expected.size(), actual.size(), "linked random size");
        }

        for (int i = 0; i < expected.size(); i++) {
            checkEquals(expected.get(i), actual.get(i), "linked final content at " + i);
        }
    }

    private static void testLinkedListLargeInput() {
        LinkedList values = new LinkedList();
        for (int i = 0; i < 100_000; i++) {
            values.add(i);
        }
        checkEquals(100_000, values.size(), "linked large size");
        checkEquals(0, values.get(0), "linked large first value");
        checkEquals(50_000, values.get(50_000), "linked large middle value");
        checkEquals(99_999, values.get(99_999), "linked large last value");
    }

    private static void checkContents(DynamicArray values, int... expected) {
        checkEquals(expected.length, values.size(), "content size");
        for (int i = 0; i < expected.length; i++) {
            checkEquals(expected[i], values.get(i), "content at " + i);
        }
    }

    private static void checkContents(LinkedList values, int... expected) {
        checkEquals(expected.length, values.size(), "linked content size");
        for (int i = 0; i < expected.length; i++) {
            checkEquals(expected[i], values.get(i), "linked content at " + i);
        }
    }

    private static void expectIndexOutOfBounds(Runnable action, String description) {
        try {
            action.run();
        } catch (IndexOutOfBoundsException expected) {
            checks++;
            return;
        }
        throw new AssertionError("Expected IndexOutOfBoundsException: " + description);
    }

    private static void checkEquals(int expected, int actual, String description) {
        check(expected == actual, description + " (expected " + expected + ", got " + actual + ")");
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) {
            throw new AssertionError(description);
        }
    }
}
