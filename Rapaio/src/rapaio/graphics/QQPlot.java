package rapaio.graphics;

import rapaio.data.IndexOneVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.distributions.Distribution;
import rapaio.graphics.plot.Points;

import static rapaio.core.BaseFilters.sort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class QQPlot extends Plot {

    public QQPlot() {
        setLeftLabel("Sample Quantiles");
        setBottomLabel("Theorethical Quantiles");
    }

    public void add(Vector points, Distribution distribution) {
        Vector x = sort(points);

        Vector y = new NumericVector("pdf", x.getRowCount());
        for (int i = 0; i < y.getRowCount(); i++) {
            double p = (i + 1) / (y.getRowCount() + 1.);
            y.setValue(i, distribution.quantile(p));
        }

        Points pts = new Points(this, y, x);
        pts.opt().setColorIndex(new IndexOneVector(0));
        this.add(pts);
    }

}
