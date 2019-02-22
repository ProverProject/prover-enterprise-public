package io.prover.common.util;

public class MatrSolver {
    public double x0, x1;

    /**
     * solves equation:
     * a0 * x0 + a1 * x1 = a
     * b0 * x0 + b1 * x1 = b
     */
    public void solve(double a0, double a1, double a, double b0, double b1, double b) {
        double def = a0 * b1 - a1 * b0;
        double def0 = a * b1 - a1 * b;
        double def1 = a0 * b - a * b0;
        x0 = def0 / def;
        x1 = def1 / def;
    }

    public void solve(float a0, float a1, float a, float b0, float b1, float b) {
        float def = a0 * b1 - a1 * b0;
        float def0 = a * b1 - a1 * b;
        float def1 = a0 * b - a * b0;
        x0 = def0 / def;
        x1 = def1 / def;
    }
}