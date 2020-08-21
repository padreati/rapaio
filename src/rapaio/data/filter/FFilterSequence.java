package rapaio.data.filter;

import rapaio.data.Frame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class FFilterSequence implements FFilter {

    public static FFilterSequence of(FFilter... filters) {
        return new FFilterSequence(filters);
    }

    private static final long serialVersionUID = -4180868104157224336L;
    private final List<FFilter> filters;
    private String[] varNames = new String[0];

    private FFilterSequence(FFilter... filterArray) {
        filters = Arrays.asList(filterArray);
    }

    @Override
    public FFilter newInstance() {
        FFilter[] filterArray = new FFilter[filters.size()];
        for (int i = 0; i < filterArray.length; i++) {
            filterArray[i] = filters.get(i).newInstance();
        }
        return new FFilterSequence(filterArray);
    }

    @Override
    public String[] varNames() {
        return varNames;
    }

    @Override
    public void fit(Frame df) {
        HashSet<String> set = new HashSet<>();
        for (FFilter filter : filters) {
            filter.fit(df);
            set.addAll(Arrays.asList(filter.varNames()));
        }
        varNames = set.toArray(new String[0]);
    }

    @Override
    public Frame apply(Frame df) {
        var result = df;
        for (var filter : filters) {
            result = filter.apply(result);
        }
        return result;
    }
}
