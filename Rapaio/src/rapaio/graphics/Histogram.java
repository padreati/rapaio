package rapaio.graphics;

import rapaio.data.Vector;
import rapaio.graphics.plot.HistogramBars;

/**
 * @author tutuianu
 */
public class Histogram extends Plot {

    private HistogramBars hist;

    public Histogram(Vector v) {
        this(v, 20, false);
    }

    public Histogram(Vector v, int bins, boolean prob) {
        this(v, bins, prob, Double.NaN, Double.NaN);
    }

    public Histogram(Vector v, int bins, boolean prob, double from, double to) {
        hist = new HistogramBars(this, v, bins, prob, from, to);
        add(hist);
        this.setBottomLabel(v.getName());
    }

    public int getBins() {
        return hist.getBins();
    }

    public void setBins(int bins) {
        hist.setBins(bins);
        hist.setRebuild(true);
    }

    public boolean isProb() {
        return hist.isProb();
    }

    public void setProb(boolean prob) {
        hist.setProb(prob);
        hist.setRebuild(true);
    }
}
