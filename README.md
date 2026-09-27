# Assignment 2: Data Structures

## 1. Overview

I implemented a Dynamic Array, a singly Linked List, and a Min-Heap in Java. They store `int` values. The array keeps values in one backing array, the list connects nodes, and the heap keeps the smallest value at the root. I use Java collections only in the tests to check my results.

## 2. Complexity Analysis

Here `n` is the current number of elements. `Θ(f(n))` gives a tight bound, so it also gives an upper bound `O(f(n))` and a lower bound `Ω(f(n))`. Best, average, and worst describe different cases of an operation. For averages below, indices are chosen uniformly; half of search queries miss and half find a value at a uniformly chosen position. The best case for append or heap insertion assumes spare capacity.

### Dynamic Array

| Operation | Best | Average | Worst | Extra space |
| --- | --- | --- | --- | --- |
| `add(x)` | `Θ(1)` | `Θ(1)` amortized | `Θ(n)` on resize | `Θ(1)`, or `Θ(n)` on resize |
| `add(index, x)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)`, or `Θ(n)` on resize |
| `remove(index)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| `get(index)` | `Θ(1)` | `Θ(1)` | `Θ(1)` | `Θ(1)` |
| `contains(x)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |

`get(index)` reads one position. Inserting or removing near the front shifts many values. Appending usually takes constant time, but copying into a larger array costs `Θ(n)` when it is full. Over many appends, that copying averages to `Θ(1)` per call (amortized).

### Linked List

| Operation | Best | Average | Worst | Extra space |
| --- | --- | --- | --- | --- |
| `add(x)` | `Θ(1)` | `Θ(1)` | `Θ(1)` | `Θ(1)` |
| `add(index, x)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| `remove(index)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| `get(index)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |
| `contains(x)` | `Θ(1)` | `Θ(n)` | `Θ(n)` | `Θ(1)` |

I keep a `tail` reference, so `add(x)` does not need a traversal. Adding or removing at the front is also constant time. For an index in the middle, the list must follow links from `head`. Removing the last node is linear because this singly linked list must find the node before it.

### Min-Heap

| Operation | Best | Average | Worst | Extra space |
| --- | --- | --- | --- | --- |
| `insert(x)` | `Θ(1)` | expected `Θ(1)`* | `Θ(log n)`, or `Θ(n)` on resize | `Θ(1)`, or `Θ(n)` on resize |
| `peekMin()` | `Θ(1)` | `Θ(1)` | `Θ(1)` | `Θ(1)` |
| `extractMin()` | `Θ(1)` | expected `Θ(log n)`* | `Θ(log n)` | `Θ(1)` |

The heap has about `log₂ n` levels. `peekMin()` reads the root, while insertion may move up and extraction may move down one path. A full backing array also has to grow during some insertions. *The average figures assume random distinct priorities; the cost of resizing is averaged over many insertions.*

## 3. Correctness: Loop Invariants

### Dynamic Array: `add(index, x)`

**Invariant:** At the start of a shift-loop iteration with position `i`, elements before `i` still have their original values, and elements after `i` have already shifted right by one.

**Initialization:** The loop starts at the old `size`. Nothing has shifted yet. If more capacity was needed, the old values were copied unchanged.

**Maintenance:** `elements[i] = elements[i - 1]` copies a value that has not moved yet. Decreasing `i` makes the shifted part one position larger, so the invariant stays true.

**Termination:** `i - index` decreases on each iteration. At `i = index`, the old suffix has shifted right and the prefix is unchanged. Writing `x` at `index` and increasing `size` gives exactly the requested insertion, including insertion at the end.

### Linked List: `contains(x)`

**Invariant:** Before checking `current`, none of the nodes already visited contains `x`, and `current` is the next node to check.

**Initialization:** `current` starts at `head`; no nodes have been checked.

**Maintenance:** If `current.value == x`, returning `true` is correct. Otherwise this node does not match, and moving to `current.next` keeps the invariant true.

**Termination:** Each unsuccessful step moves to the next node in a finite list. When `current` becomes `null`, every node has been checked, so returning `false` is correct. Therefore the method returns `true` exactly when `x` is in the list.

## 4. Tests and Running the Code

`Tests.java` checks empty structures, duplicates, invalid indices, large inputs, and heap order. It also compares mixed operations with Java's `ArrayList`, `LinkedList`, and `PriorityQueue`.

```text
javac -d out src/DynamicArray.java src/LinkedList.java src/MinHeap.java src/Tests.java
java -cp out Tests
```

## 5. Current Status

The three structures, tests, complexity tables, and two proofs are done. Benchmark timings, plots, and the discussion will be added after the experiments.
