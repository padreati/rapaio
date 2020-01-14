package rapaio.math.linear;

import rapaio.data.SolidFrame;
import rapaio.util.function.DoubleDoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDVector implements DVector {

    private static final long serialVersionUID = 4164614372206348682L;

    protected void checkConformance(DVector vector) {
        if (size() != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Vectors are not conform for operation: [%d] vs [%d]", size(), vector.size()));
        }
    }

    @Override
    public DVector plus(double x) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + x);
        }
        return this;
    }

    @Override
    public DVector plus(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + b.get(i));
        }
        return this;
    }

    @Override
    public DVector minus(double x) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) - x);
        }
        return this;
    }

    @Override
    public DVector minus(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) - b.get(i));
        }
        return this;
    }

    @Override
    public DVector times(double scalar) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * scalar);
        }
        return this;
    }

    @Override
    public DVector times(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * b.get(i));
        }
        return this;
    }

    @Override
    public DVector div(double scalar) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / scalar);
        }
        return this;
    }

    @Override
    public DVector div(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / b.get(i));
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        double s = 0;
        for (int i = 0; i < size(); i++) {
            s = Math.fma(get(i), b.get(i), s);
        }
        return s;
    }

    public double norm(double p) {
        if (p <= 0) {
            return size();
        }
        if (p == Double.POSITIVE_INFINITY) {
            double max = Double.NaN;
            for (int i = 0; i < size(); i++) {
                double value = get(i);
                if (Double.isNaN(max)) {
                    max = value;
                } else {
                    max = Math.max(max, value);
                }
            }
            return max;
        }

        double s = 0.0;
        for (int i = 0; i < size(); i++) {
            s += Math.pow(Math.abs(get(i)), p);
        }
        return Math.pow(s, 1.0 / p);
    }

    public DVector normalize(double p) {
        double norm = norm(p);
        if (norm != 0.0)
            times(1.0 / norm);
        return this;
    }

    @Override
    public double sum() {
        double sum = 0;
        for (int i = 0; i < size(); i++) {
            sum += get(i);
        }
        return sum;
    }

    @Override
    public double nansum() {
        double nansum = 0;
        for (int i = 0; i < size(); i++) {
            double value = get(i);
            if (Double.isNaN(value)) {
                continue;
            }
            nansum += value;
        }
        return nansum;
    }

    @Override
    public int nancount() {
        int nancount = 0;
        for (int i = 0; i < size(); i++) {
            double value = get(i);
            if (Double.isNaN(value)) {
                continue;
            }
            nancount++;
        }
        return nancount;
    }

    @Override
    public double mean() {
        return sum() / size();
    }

    @Override
    public double nanmean() {
        return nansum() / nancount();
    }

    @Override
    public double variance() {
        if (size() == 0) {
            return Double.NaN;
        }
        double mean = mean();
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < size(); i++) {
            sum2 += Math.pow(get(i) - mean, 2);
            sum3 += get(i) - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / size()) / (size() - 1.0);
    }

    @Override
    public double nanvariance() {
        double mean = nanmean();
        int missingCount = 0;
        int completeCount = 0;
        for (int i = 0; i < size(); i++) {
            if (Double.isNaN(get(i))) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (completeCount == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < size(); i++) {
            if (Double.isNaN(get(i))) {
                continue;
            }
            sum2 += Math.pow(get(i) - mean, 2);
            sum3 += get(i) - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / completeCount) / (completeCount - 1.0);
    }

    @Override
    public DVector apply(DoubleDoubleFunction f) {
        for (int i = 0; i < size(); i++) {
            set(i, f.applyAsDouble(get(i)));
        }
        return this;
    }

    public String toSummary() {
        return SolidFrame.byVars(asVarDouble()).toFullContent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RV[").append(size()).append("]{");
        for (int i = 0; i < size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(get(i));
            if (i > 10) {
                sb.append("...");
                break;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
