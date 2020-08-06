package rapaio.printer.opt;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/20.
 */
public class POptionTextWidth implements POption<Integer> {

    private static final long serialVersionUID = 2485016171417227463L;
    private final int textWidth;

    public POptionTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    @Override
    public void bind(POpts opts) {
        opts.setTextWidth(this);
    }

    @Override
    public Integer apply(POpts opts) {
        return textWidth;
    }
}
