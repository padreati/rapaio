package rapaio.ml.eval.metric;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/28/20.
 */
@Builder
@Getter
public class RegressionScore implements Printable {

    @NonNull
    private final RegressionMetric metric;
    private final double value;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegressionScore{").append("metric=").append(metric.getName());
        sb.append(",value=").append(Format.floatFlex(value)).append("}");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(metric.getName()).append(": ").append(Format.floatFlex(value)).append("\n");
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }
}
