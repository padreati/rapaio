package rapaio.graphics.plot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/27/20.
 */
@RequiredArgsConstructor
@Getter
public class Axes implements Serializable {

    private static final long serialVersionUID = -851049271916067927L;

    final Plot plot;
    final List<Artist> artistList = new ArrayList<>();
    DataRange dataRange = new DataRange();

    protected double xLimStart = Double.NaN;
    protected double xLimEnd = Double.NaN;
    protected double yLimStart = Double.NaN;
    protected double yLimEnd = Double.NaN;

    public void addArtist(Artist artist) {
        this.artistList.add(artist);
    }

    public DataRange getDataRange() {
        if (dataRange == null) {
            buildDataRange();
        }
        return dataRange;
    }

    protected void buildDataRange() {
        DataRange range = new DataRange();
        for (Artist artist : artistList) {
            artist.updateDataRange(range);
        }

        if (!Double.isFinite(range.xMin())) {
            range.xMin(0);
        }
        if (!Double.isFinite(range.xMax())) {
            range.xMax(1);
        }
        if (!Double.isFinite(range.yMin())) {
            range.yMin(0);
        }
        if (!Double.isFinite(range.yMax())) {
            range.yMax(1);
        }

        range = range.getExtendedRange();

        if (Double.isFinite(xLimStart * xLimEnd)) {
            range.xMin(xLimStart);
            range.xMax(xLimEnd);
        }
        if (Double.isFinite(yLimStart * yLimEnd)) {
            range.yMin(yLimStart);
            range.yMax(yLimEnd);
        }

        if (range.xMin() == range.xMax()) {
            range.xMin(range.xMin() - 0.5);
            range.xMax(range.xMax() + 0.5);
        }

        if (range.yMin() == range.yMax()) {
            range.yMin(range.yMin() - 0.5);
            range.yMax(range.yMax() + 0.5);
        }
        dataRange = range;
    }

    public double xMin() {
        return dataRange.xMin();
    }

    public double yMin() {
        return dataRange.yMin();
    }

    public double xMax() {
        return dataRange.xMax();
    }

    public double yMax() {
        return dataRange.yMax();
    }

    public double xScale(double x) {
        return plot.viewport.x + plot.viewport.width * (x - xMin()) / (xMax() - xMin());
    }

    public double yScale(double y) {
        return plot.viewport.y + plot.viewport.height * (1. - (y - yMin()) / (yMax() - yMin()));
    }
}
