package rapaio.graphics.base;

/**
 * @author algoshipda
 */
public class Range1D {
    private double min = Double.NaN;
    private double max = Double.NaN;

    public void union(double x) {
        if (!Double.isNaN(x) && Double.isFinite(x)) {
            if (!Double.isNaN(min)) {
                min = Math.min(min, x);
            } else {
                min = x;
            }
            if (!Double.isNaN(max)) {
                max = Math.max(max, x);
            } else {
                max = x;
            }
        }
    }

    public void union(Range1D range) {
        union(range.getMin());
        union(range.getMax());
    }

    public boolean contains(double x) {
        return min <= x && x <= max;
    }

    public double length() {
        return max - min;
    }

    public void setRange(double pMin, double pMax) {
        min = pMin;
        max = pMax;
    }

    public void setMin(double x) {
        min = x;
    }

    public void setMax(double x) {
        max = x;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getProperDecimals() {
        int decimals = 0;
        double len = length();
        while (len <= 10.0 && decimals <= 7) {
            len *= 10;
            decimals++;
        }
        return decimals;
    }
}