"""Plot the benchmark CSV produced for Assignment 2.

Usage:
    python scripts/plot_results.py results/tables/summary.csv --output-dir results/plots

Requires matplotlib (``python -m pip install matplotlib``). The CSV must have
workload, structure, phase, n, operations, avg_ms, metric_name, metric_avg,
and theory columns. Blank metric_name/metric_avg values are allowed for rows
that have timing results but no operation metric.
"""

from __future__ import annotations

import argparse
import csv
import math
from collections import defaultdict
from pathlib import Path
from statistics import mean

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter


REQUIRED_COLUMNS = {
    "workload",
    "structure",
    "phase",
    "n",
    "operations",
    "avg_ms",
    "metric_name",
    "metric_avg",
    "theory",
}


def load_results(csv_path: Path) -> list[dict]:
    with csv_path.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        missing = REQUIRED_COLUMNS - set(reader.fieldnames or ())
        if missing:
            raise ValueError(f"CSV is missing columns: {', '.join(sorted(missing))}")

        results = []
        for line_number, raw in enumerate(reader, start=2):
            try:
                n = int(raw["n"])
                operations = int(raw["operations"])
                avg_ms = float(raw["avg_ms"])
                metric_name = raw["metric_name"].strip()
                metric_text = raw["metric_avg"].strip()
                metric_avg = float(metric_text) if metric_text else None
            except (TypeError, ValueError) as error:
                raise ValueError(f"Invalid number on CSV line {line_number}: {error}") from error

            if n <= 0 or operations <= 0 or not math.isfinite(avg_ms) or avg_ms <= 0:
                raise ValueError(
                    f"n, operations, and avg_ms must be positive on CSV line {line_number}"
                )
            if metric_name and metric_avg is None:
                raise ValueError(f"metric_avg is missing on CSV line {line_number}")
            if metric_avg is not None and (not math.isfinite(metric_avg) or metric_avg < 0):
                raise ValueError(f"metric_avg must be nonnegative on CSV line {line_number}")

            results.append(
                {
                    "workload": raw["workload"].strip(),
                    "structure": raw["structure"].strip(),
                    "phase": raw["phase"].strip(),
                    "n": n,
                    "operations": operations,
                    "avg_ms": avg_ms,
                    "metric_name": metric_name,
                    "metric_avg": metric_avg,
                    "theory": raw["theory"].strip(),
                }
            )

    if not results:
        raise ValueError("CSV has no data rows")
    return results


def style_axis(ax, *, ylabel: str, input_sizes: list[int]) -> None:
    ax.set_xlabel("Input size (n)")
    ax.set_ylabel(ylabel)
    ax.grid(True, alpha=0.3)
    ax.set_xscale("log")
    ax.set_xticks(input_sizes)
    ax.xaxis.set_major_formatter(ScalarFormatter())
    ax.tick_params(axis="x", labelrotation=30)


def plot_execution_time(results: list[dict], output_path: Path) -> None:
    panels = sorted({(row["workload"], row["phase"]) for row in results})
    columns = min(3, len(panels))
    rows = math.ceil(len(panels) / columns)
    fig, axes = plt.subplots(
        rows,
        columns,
        figsize=(5.2 * columns, 3.7 * rows),
        squeeze=False,
        constrained_layout=True,
    )

    for ax, (workload, phase) in zip(axes.flat, panels):
        series = defaultdict(list)
        for row in results:
            if row["workload"] == workload and row["phase"] == phase:
                series[(row["structure"], row["n"])].append(row["avg_ms"])

        structures = sorted({structure for structure, _ in series})
        for structure in structures:
            points = sorted(
                (n, mean(series[(structure, n)]))
                for name, n in series
                if name == structure
            )
            ax.plot(*zip(*points), marker="o", linewidth=1.8, markersize=4, label=structure)

        ax.set_title(f"{workload} · {phase}")
        ax.set_yscale("log")
        style_axis(
            ax,
            ylabel="Average total time (ms, 5 runs) (log scale)",
            input_sizes=sorted({n for _, n in series}),
        )
        ax.legend(fontsize="small")

    for ax in list(axes.flat)[len(panels) :]:
        ax.set_visible(False)

    fig.suptitle("Execution time vs input size", fontsize=15)
    fig.savefig(output_path, dpi=180)
    plt.close(fig)


def plot_metrics(results: list[dict], output_path: Path) -> None:
    metric_names = sorted({row["metric_name"] for row in results if row["metric_name"]})
    if not metric_names:
        raise ValueError("CSV has no metric_name / metric_avg data to plot")

    fig, axes = plt.subplots(
        len(metric_names),
        1,
        figsize=(11, max(4, 3.8 * len(metric_names))),
        squeeze=False,
        constrained_layout=True,
    )

    for ax, metric_name in zip(axes.flat, metric_names):
        series = defaultdict(list)
        for row in results:
            if row["metric_name"] == metric_name and row["metric_avg"] is not None:
                key = (row["workload"], row["phase"], row["structure"], row["n"])
                series[key].append(row["metric_avg"])

        labels = sorted({key[:3] for key in series})
        for workload, phase, structure in labels:
            points = sorted(
                (n, mean(values))
                for (w, p, s, n), values in series.items()
                if (w, p, s) == (workload, phase, structure)
            )
            ax.plot(
                *zip(*points),
                marker="o",
                linewidth=1.8,
                markersize=4,
                label=f"{workload} / {phase} / {structure}",
            )

        ax.set_title(metric_name.replace("_", " ").title())
        style_axis(
            ax,
            ylabel="Count for full batch",
            input_sizes=sorted({n for _, _, _, n in series}),
        )
        ax.legend(loc="upper left", bbox_to_anchor=(1.01, 1), fontsize="small")

    fig.suptitle("Operation metrics vs input size", fontsize=15)
    fig.savefig(output_path, dpi=180)
    plt.close(fig)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("csv", type=Path, help="Benchmark results CSV")
    parser.add_argument(
        "--output-dir",
        type=Path,
        help="Directory for PNG charts (default: a plots directory beside the CSV)",
    )
    args = parser.parse_args()

    results = load_results(args.csv)
    output_dir = args.output_dir or args.csv.parent / "plots"
    output_dir.mkdir(parents=True, exist_ok=True)
    time_path = output_dir / "execution_time_vs_n.png"
    metrics_path = output_dir / "metrics_vs_n.png"
    plot_execution_time(results, time_path)
    plot_metrics(results, metrics_path)
    print(f"Saved {time_path}")
    print(f"Saved {metrics_path}")


if __name__ == "__main__":
    main()
