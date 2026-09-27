"""Draw the two charts for the benchmark results.

Run: python scripts/plot_results.py results/tables/summary.csv --output-dir results/plots
"""

import argparse
import csv
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter


def read_results(path):
    with path.open(newline="", encoding="utf-8") as file:
        rows = list(csv.DictReader(file))

    for row in rows:
        row["n"] = int(row["n"])
        row["avg_ms"] = float(row["avg_ms"])
        row["metric_avg"] = float(row["metric_avg"])
    return rows


def set_axes(ax, sizes, ylabel):
    ax.set_xlabel("Input size (n)")
    ax.set_ylabel(ylabel)
    ax.set_xscale("log")
    ax.set_xticks(sizes)
    ax.xaxis.set_major_formatter(ScalarFormatter())
    ax.tick_params(axis="x", labelrotation=30)
    ax.grid(True, alpha=0.3)


def plot_times(rows, output_path):
    phases = sorted({row["phase"] for row in rows})
    sizes = sorted({row["n"] for row in rows})
    fig, axes = plt.subplots(3, 3, figsize=(15, 11), constrained_layout=True)

    for ax, phase in zip(axes.flat, phases):
        phase_rows = [row for row in rows if row["phase"] == phase]
        structures = sorted({row["structure"] for row in phase_rows})
        for structure in structures:
            points = sorted(
                (row["n"], row["avg_ms"])
                for row in phase_rows if row["structure"] == structure
            )
            x, y = zip(*points)
            ax.plot(x, y, marker="o", label=structure)

        ax.set_title(phase.replace("_", " "))
        ax.set_yscale("log")
        set_axes(ax, sizes, "Average batch time (ms, 5 runs)")
        ax.legend(fontsize="small")

    for ax in list(axes.flat)[len(phases):]:
        ax.set_visible(False)

    fig.suptitle("Execution time vs input size")
    fig.savefig(output_path, dpi=180)
    plt.close(fig)


def plot_counts(rows, output_path):
    metrics = sorted({row["metric_name"] for row in rows})
    sizes = sorted({row["n"] for row in rows})
    fig, axes = plt.subplots(len(metrics), 1, figsize=(11, 4 * len(metrics)),
                             constrained_layout=True)

    for ax, metric in zip(axes, metrics):
        metric_rows = [row for row in rows if row["metric_name"] == metric]
        series = sorted({(row["phase"], row["structure"]) for row in metric_rows})
        for phase, structure in series:
            points = sorted(
                (row["n"], row["metric_avg"])
                for row in metric_rows
                if row["phase"] == phase and row["structure"] == structure
            )
            x, y = zip(*points)
            ax.plot(x, y, marker="o", label=f"{phase}: {structure}")

        ax.set_title(metric.capitalize())
        set_axes(ax, sizes, "Count for the whole batch")
        ax.legend(fontsize="small")

    fig.suptitle("Operation counts vs input size")
    fig.savefig(output_path, dpi=180)
    plt.close(fig)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("csv", type=Path)
    parser.add_argument("--output-dir", type=Path, default=Path("results/plots"))
    args = parser.parse_args()

    rows = read_results(args.csv)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    plot_times(rows, args.output_dir / "execution_time_vs_n.png")
    plot_counts(rows, args.output_dir / "metrics_vs_n.png")
    print("Saved both charts to", args.output_dir)


if __name__ == "__main__":
    main()
