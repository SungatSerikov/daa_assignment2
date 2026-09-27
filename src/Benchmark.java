import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

public final class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int RUNS = 5;
    private static final int GET_COUNT = 10_000;
    private static final int SEARCH_COUNT = 1_000;
    private static final int UPDATE_COUNT = 1_000;
    private static volatile long checksum;

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        Path tableDirectory = Path.of("results", "tables");
        Files.createDirectories(tableDirectory);
        warmUp();

        try (PrintWriter summary = new PrintWriter(Files.newBufferedWriter(tableDirectory.resolve("summary.csv")));
             PrintWriter runs = new PrintWriter(Files.newBufferedWriter(tableDirectory.resolve("runs.csv")))) {
            summary.println("workload,structure,phase,n,operations,avg_ms,metric_name,metric_avg,theory");
            runs.println("workload,structure,phase,n,run,elapsed_ns");

            for (int n : SIZES) {
                Random random = new Random(42);
                int[] values = randomUniquePositiveValues(n, random);
                int[] getIndices = randomIndices(GET_COUNT, n, random);
                int[] searchValues = searchValues(values, random);
                int[] insertedValues = randomValues(UPDATE_COUNT, random);

                benchmarkGet(n, values, getIndices, summary, runs);
                benchmarkContains(n, values, searchValues, summary, runs);
                benchmarkUpdates(n, values, insertedValues, summary, runs);
                benchmarkHeap(n, values, summary, runs);
                System.out.println("Finished n=" + n);
            }
        }
        System.out.println("Saved results/tables/summary.csv and runs.csv (checksum=" + checksum + ")");
    }

    private static void benchmarkGet(int n, int[] values, int[] indices,
                                     PrintWriter summary, PrintWriter runs) {
        long[] arrayTimes = new long[RUNS];
        long[] listTimes = new long[RUNS];
        long listAccesses = 0;
        for (int index : indices) {
            listAccesses += index + 1L;
        }

        for (int run = 0; run < RUNS; run++) {
            DynamicArray array = makeArray(values);
            long sum = 0;
            long start = System.nanoTime();
            for (int index : indices) {
                sum += array.get(index);
            }
            arrayTimes[run] = System.nanoTime() - start;
            checksum += sum;

            LinkedList list = makeList(values);
            sum = 0;
            start = System.nanoTime();
            for (int index : indices) {
                sum += list.get(index);
            }
            listTimes[run] = System.nanoTime() - start;
            checksum += sum;
        }

        record(summary, runs, "random_access", "DynamicArray", "get", n,
                GET_COUNT, arrayTimes, "element accesses", GET_COUNT, "Theta(m)");
        record(summary, runs, "random_access", "LinkedList", "get", n,
                GET_COUNT, listTimes, "element accesses", listAccesses, "Theta(m*n)");
    }

    private static void benchmarkContains(int n, int[] values, int[] queries,
                                          PrintWriter summary, PrintWriter runs) {
        long[] arrayTimes = new long[RUNS];
        long[] listTimes = new long[RUNS];
        long comparisons = searchComparisons(values, queries);

        for (int run = 0; run < RUNS; run++) {
            DynamicArray array = makeArray(values);
            int found = 0;
            long start = System.nanoTime();
            for (int query : queries) {
                if (array.contains(query)) {
                    found++;
                }
            }
            arrayTimes[run] = System.nanoTime() - start;
            if (found != SEARCH_COUNT / 2) {
                throw new AssertionError("Unexpected array search result");
            }
            checksum += found;

            LinkedList list = makeList(values);
            found = 0;
            start = System.nanoTime();
            for (int query : queries) {
                if (list.contains(query)) {
                    found++;
                }
            }
            listTimes[run] = System.nanoTime() - start;
            if (found != SEARCH_COUNT / 2) {
                throw new AssertionError("Unexpected list search result");
            }
            checksum += found;
        }

        record(summary, runs, "search", "DynamicArray", "contains", n,
                SEARCH_COUNT, arrayTimes, "comparisons", comparisons, "Theta(m*n)");
        record(summary, runs, "search", "LinkedList", "contains", n,
                SEARCH_COUNT, listTimes, "comparisons", comparisons, "Theta(m*n)");
    }

    private static void benchmarkUpdates(int n, int[] values, int[] additions,
                                         PrintWriter summary, PrintWriter runs) {
        int[] positions = {0, n / 2};
        String[] names = {"front", "middle"};
        for (int part = 0; part < positions.length; part++) {
            int index = positions[part];
            String name = names[part];

            long[] arrayInserts = new long[RUNS];
            long[] listInserts = new long[RUNS];
            long[] arrayRemovals = new long[RUNS];
            long[] listRemovals = new long[RUNS];
            for (int run = 0; run < RUNS; run++) {
                arrayInserts[run] = timeArrayInserts(values, additions, index);
                listInserts[run] = timeListInserts(values, additions, index);
                arrayRemovals[run] = timeArrayRemovals(values, index);
                listRemovals[run] = timeListRemovals(values, index);
            }

            record(summary, runs, "update", "DynamicArray", "insert_" + name, n,
                    UPDATE_COUNT, arrayInserts, "element movements",
                    arrayInsertionMovements(n, index), "O(m*n + m^2)");
            record(summary, runs, "update", "LinkedList", "insert_" + name, n,
                    UPDATE_COUNT, listInserts, "node visits",
                    (long) UPDATE_COUNT * index, index == 0 ? "Theta(m)" : "Theta(m*n)");
            record(summary, runs, "update", "DynamicArray", "remove_" + name, n,
                    UPDATE_COUNT, arrayRemovals, "element movements",
                    arrayRemovalMovements(n, index), "O(m*n)");
            record(summary, runs, "update", "LinkedList", "remove_" + name, n,
                    UPDATE_COUNT, listRemovals, "node visits",
                    (long) UPDATE_COUNT * (index + 1), index == 0 ? "Theta(m)" : "Theta(m*n)");
        }
    }

    private static long timeArrayInserts(int[] values, int[] additions, int index) {
        DynamicArray array = makeArray(values);
        long start = System.nanoTime();
        for (int value : additions) {
            array.add(index, value);
        }
        long elapsed = System.nanoTime() - start;
        if (array.size() != values.length + UPDATE_COUNT) {
            throw new AssertionError("Array insert size");
        }
        checksum += array.get(index);
        return elapsed;
    }

    private static long timeListInserts(int[] values, int[] additions, int index) {
        LinkedList list = makeList(values);
        long start = System.nanoTime();
        for (int value : additions) {
            list.add(index, value);
        }
        long elapsed = System.nanoTime() - start;
        if (list.size() != values.length + UPDATE_COUNT) {
            throw new AssertionError("List insert size");
        }
        checksum += list.get(index);
        return elapsed;
    }

    // Rebuild outside the timed section when the original n elements are exhausted.
    private static long timeArrayRemovals(int[] values, int index) {
        int remaining = UPDATE_COUNT;
        long elapsed = 0;
        long sum = 0;
        while (remaining > 0) {
            DynamicArray array = makeArray(values);
            int batch = Math.min(remaining, values.length - index);
            long start = System.nanoTime();
            for (int i = 0; i < batch; i++) {
                sum += array.remove(index);
            }
            elapsed += System.nanoTime() - start;
            remaining -= batch;
        }
        checksum += sum;
        return elapsed;
    }

    private static long timeListRemovals(int[] values, int index) {
        int remaining = UPDATE_COUNT;
        long elapsed = 0;
        long sum = 0;
        while (remaining > 0) {
            LinkedList list = makeList(values);
            int batch = Math.min(remaining, values.length - index);
            long start = System.nanoTime();
            for (int i = 0; i < batch; i++) {
                sum += list.remove(index);
            }
            elapsed += System.nanoTime() - start;
            remaining -= batch;
        }
        checksum += sum;
        return elapsed;
    }

    private static void benchmarkHeap(int n, int[] values, PrintWriter summary, PrintWriter runs) {
        long[] insertTimes = new long[RUNS];
        long[] extractTimes = new long[RUNS];
        long[] insertComparisons = new long[RUNS];
        long[] extractComparisons = new long[RUNS];

        for (int run = 0; run < RUNS; run++) {
            MinHeap heap = new MinHeap();
            int[] extracted = new int[n];
            long start = System.nanoTime();
            for (int value : values) {
                heap.insert(value);
            }
            insertTimes[run] = System.nanoTime() - start;
            insertComparisons[run] = heap.comparisonCount();
            if (heap.size() != n) {
                throw new AssertionError("Heap after insertion");
            }

            heap.resetComparisonCount();
            start = System.nanoTime();
            for (int i = 0; i < n; i++) {
                extracted[i] = heap.extractMin();
            }
            extractTimes[run] = System.nanoTime() - start;
            extractComparisons[run] = heap.comparisonCount();
            if (heap.size() != 0) {
                throw new AssertionError("Heap after extraction");
            }
            for (int i = 1; i < n; i++) {
                if (extracted[i - 1] > extracted[i]) {
                    throw new AssertionError("Extraction order");
                }
            }
            checksum += extracted[0] + (long) extracted[n - 1];
        }

        record(summary, runs, "heap", "MinHeap", "insert", n, n, insertTimes,
                "heap comparisons", mean(insertComparisons), "O(n log n)");
        record(summary, runs, "heap", "MinHeap", "extract", n, n, extractTimes,
                "heap comparisons", mean(extractComparisons), "O(n log n)");
    }

    private static long arrayInsertionMovements(int n, int index) {
        int size = n;
        int capacity = 4;
        while (capacity < n) {
            capacity *= 2;
        }
        long movements = 0;
        for (int i = 0; i < UPDATE_COUNT; i++) {
            if (size == capacity) {
                movements += size; // Copy into the doubled backing array.
                capacity *= 2;
            }
            movements += size - index; // Shift the suffix right.
            size++;
        }
        return movements;
    }

    private static long arrayRemovalMovements(int n, int index) {
        int remaining = UPDATE_COUNT;
        long movements = 0;
        while (remaining > 0) {
            int size = n;
            int batch = Math.min(remaining, n - index);
            for (int i = 0; i < batch; i++) {
                movements += size - index - 1;
                size--;
            }
            remaining -= batch;
        }
        return movements;
    }

    private static long searchComparisons(int[] values, int[] queries) {
        HashMap<Integer, Integer> positions = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            positions.put(values[i], i);
        }
        long total = 0;
        for (int query : queries) {
            Integer position = positions.get(query);
            total += position == null ? values.length : position + 1L;
        }
        return total;
    }

    private static int[] randomUniquePositiveValues(int n, Random random) {
        int[] values = new int[n];
        HashSet<Integer> used = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int value;
            do {
                value = 1 + random.nextInt(Integer.MAX_VALUE - 1);
            } while (!used.add(value));
            values[i] = value;
        }
        return values;
    }

    private static int[] randomIndices(int count, int n, Random random) {
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = random.nextInt(n);
        }
        return indices;
    }

    private static int[] searchValues(int[] values, Random random) {
        int[] queries = new int[SEARCH_COUNT];
        for (int i = 0; i < SEARCH_COUNT / 2; i++) {
            queries[i] = values[random.nextInt(values.length)];
        }
        for (int i = SEARCH_COUNT / 2; i < SEARCH_COUNT; i++) {
            queries[i] = -1 - random.nextInt(values.length);
        }
        for (int i = queries.length - 1; i > 0; i--) {
            int other = random.nextInt(i + 1);
            int temporary = queries[i];
            queries[i] = queries[other];
            queries[other] = temporary;
        }
        return queries;
    }

    private static int[] randomValues(int count, Random random) {
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = random.nextInt();
        }
        return values;
    }

    private static DynamicArray makeArray(int[] values) {
        DynamicArray array = new DynamicArray();
        for (int value : values) {
            array.add(value);
        }
        return array;
    }

    private static LinkedList makeList(int[] values) {
        LinkedList list = new LinkedList();
        for (int value : values) {
            list.add(value);
        }
        return list;
    }

    private static double mean(long[] values) {
        double total = 0;
        for (long value : values) {
            total += value;
        }
        return total / values.length;
    }

    private static void record(PrintWriter summary, PrintWriter runs, String workload,
                               String structure, String phase, int n, int operations,
                               long[] times, String metricName, double metric, String theory) {
        for (int i = 0; i < times.length; i++) {
            runs.printf(Locale.US, "%s,%s,%s,%d,%d,%d%n",
                    workload, structure, phase, n, i + 1, times[i]);
        }
        summary.printf(Locale.US, "%s,%s,%s,%d,%d,%.6f,%s,%.0f,%s%n",
                workload, structure, phase, n, operations, mean(times) / 1_000_000.0,
                metricName, metric, theory);
    }

    private static void warmUp() {
        int n = 1_000;
        Random random = new Random(42);
        int[] values = randomUniquePositiveValues(n, random);
        int[] indices = randomIndices(GET_COUNT, n, random);
        int[] queries = searchValues(values, random);
        int[] additions = randomValues(UPDATE_COUNT, random);
        PrintWriter discarded = new PrintWriter(new StringWriter());
        for (int repeat = 0; repeat < 3; repeat++) {
            benchmarkGet(n, values, indices, discarded, discarded);
            benchmarkContains(n, values, queries, discarded, discarded);
            benchmarkUpdates(n, values, additions, discarded, discarded);
            benchmarkHeap(n, values, discarded, discarded);
        }
    }
}
