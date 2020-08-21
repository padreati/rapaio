package rapaio.data.filter;

import rapaio.data.Var;

import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class VFilterSequence implements VFilter {

    private static final long serialVersionUID = -2495739155198558730L;
    private final List<VFilter> filters;

    private VFilterSequence(VFilter... filterArray) {
        filters = Arrays.asList(filterArray);
    }

    @Override
    public VFilter fit(Var var) {
        for (VFilter filter : filters) {
            filter.fit(var);
        }
        return this;
    }

    @Override
    public Var apply(Var var) {
        Var result = var;
        for (VFilter filter : filters) {
            result = filter.apply(result);
        }
        return result;
    }

    @Override
    public Var fapply(Var var) {
        Var result = var;
        for (VFilter filter : filters) {
            result = filter.fapply(result);
        }
        return result;
    }
}
