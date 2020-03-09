package rapaio.printer.opt;

import java.text.DecimalFormat;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/9/20.
 */
public class POtpionFloatFormat implements POption<DecimalFormat> {

    private static final long serialVersionUID = -6426137730862137730L;

    private DecimalFormat format;

    public POtpionFloatFormat(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void bind(POpts opts) {
        opts.setFloatFormat(this);
    }

    @Override
    public DecimalFormat apply(POpts opts) {
        return format;
    }
}
