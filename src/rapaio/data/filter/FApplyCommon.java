package rapaio.data.filter;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarBinary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FApplyCommon {

    public Frame applyHotEncoding(Frame df,String[] varNames,Map<String, List<String>> levels, boolean useNa,boolean lessOne) {

        if (varNames == null || varNames.length == 0) {
            return df;
        }

        // list of variables with encoding
        List<Var> vars = new ArrayList<>();

        for (String varName : df.varNames()) {

            // if the variable has been learned
            if (levels.containsKey(varName)) {

                // get the learned dictionary
                List<String> dict = levels.get(varName);
                if (!useNa) {
                    dict = dict.subList(1, dict.size());
                }
                if (lessOne) {
                    dict = dict.subList(1, dict.size());
                }

                List<Var> oneHotVars = new ArrayList<>();
                Map<String, Var> index = new HashMap<>();

                // create a new numeric var for each level, filled with 0
                for (String token : dict) {
                    Var v = VarBinary.fill(df.rowCount(), 0).name(varName + "." + token);
                    oneHotVars.add(v);
                    index.put(token, v);
                }
                // populate encoding variables
                for (int i = 0; i < df.rowCount(); i++) {
                    String level = df.getLabel(i, varName);
                    if (index.containsKey(level)) {
                        index.get(level).setInt(i, 1);
                    }
                }
                vars.addAll(oneHotVars);
            } else {
                vars.add(df.rvar(varName));
            }
        }
        return BoundFrame.byVars(vars);
    }

}
