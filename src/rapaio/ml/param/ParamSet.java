package rapaio.ml.param;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/29/20.
 */
public abstract class ParamSet {

    private final Map<String, Param<?, ? extends ParamSet>> parameterList = new HashMap<>();

    public ParamSet() {
    }

    public ParamSet(ParamSet source) {
        this.copyParameterValues(source);
    }

    public void registerParameter(Param<?, ? extends ParamSet> parameter) {
        if (parameterList.containsKey(parameter.name())) {
            throw new IllegalArgumentException("Parameters contains a prameter with the same name.");
        }
        parameterList.put(parameter.name(), parameter);
    }

    protected void copyParameterValues(ParamSet paramSet) {
        for (var e : parameterList.entrySet()) {
            e.getValue().copyFrom(paramSet.parameterList.get(e.getKey()));
        }
    }
}
