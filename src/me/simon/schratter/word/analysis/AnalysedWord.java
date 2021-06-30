package me.simon.schratter.word.analysis;

public class AnalysedWord {

    private int count;
    private double frequency;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void calculateFrequency(double total) {
        this.frequency = count / total;
    }

    public void increment() {
        count++;
    }

    @Override
    public String toString() {
        return "AnalysedWord{" +
                "count=" + count +
                ", frequency=" + frequency +
                '}';
    }
}

