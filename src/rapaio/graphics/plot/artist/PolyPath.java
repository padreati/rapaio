package rapaio.graphics.plot.artist;

import rapaio.data.VarDouble;
import rapaio.util.Pair;

public final class PolyPath {

    private final VarDouble xs = VarDouble.empty();
    private final VarDouble ys = VarDouble.empty();

    public PolyPath addPoints(VarDouble x, VarDouble y, boolean reverse) {
        int n = Math.min(x.size(), y.size());
        if (reverse) {
            for (int i = n - 1; i >= 0; i--) {
                xs.addDouble(x.getDouble(i));
                ys.addDouble(y.getDouble(i));
            }
        } else {
            for (int i = 0; i < n; i++) {
                xs.addDouble(x.getDouble(i));
                ys.addDouble(y.getDouble(i));
            }
        }
        return this;
    }

    public Pair<VarDouble, VarDouble> getPath() {
        return Pair.from(xs, ys);
    }
}
