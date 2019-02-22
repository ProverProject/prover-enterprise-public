package io.prover.swypeid.util;

public class StatCounter {
    private int min = Integer.MAX_VALUE;
    private int max = 0;
    private int sum = 0;
    private int amount = 0;

    public void add(int value) {
        sum += value;
        ++amount;
        if (value > max)
            max = value;
        if (value < min)
            min = value;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public double avg() {
        return sum / (double) amount;
    }

    public void clear() {
        min = Integer.MAX_VALUE;
        max = 0;
        sum = 0;
        amount = 0;
    }
}
