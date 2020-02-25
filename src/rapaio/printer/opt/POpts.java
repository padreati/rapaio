package rapaio.printer.opt;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Printing options.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/20.
 */
public class POpts implements Serializable {

    private static final long serialVersionUID = -2369999674228369814L;

    private static final POpts defaults;

    static {
        defaults = new POpts(null);
        defaults.setTextWidth(new POptionTextWidth(120));
    }

    public POpts(POpts parent) {
        this.parent = parent;
    }

    private POpts parent;
    private POptionTextWidth textWidth;


    public POpts getParent() {
        return parent;
    }

    public void setParent(POpts parent) {
        this.parent = parent;
    }

    public POpts bind(POption... options) {
        Arrays.stream(options).forEach(o -> o.bind(this));
        return this;
    }

    public POption[] toArray() {
        return new POption[]{
                textWidth
        };
    }

    public int textWidth() {
        if (textWidth == null) {
            return parent != null ? parent.textWidth() : defaults.textWidth.apply(this);
        }
        return textWidth.apply(this);
    }

    public void setTextWidth(POptionTextWidth textWidth) {
        this.textWidth = textWidth;
    }
}
