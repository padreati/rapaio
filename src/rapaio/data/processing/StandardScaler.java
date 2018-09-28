/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.data.processing;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/21/18.
 */
public class StandardScaler implements Scaler {

    public static StandardScaler from(Frame df, boolean center, boolean scale) {
        StandardScaler standardScaler = new StandardScaler(center, scale);
        standardScaler.train(df);
        return standardScaler;
    }

    private final boolean center;
    private final boolean scale;

    private HashSet<String> varNames = new HashSet<>();
    private HashMap<String, Double> mean = new HashMap<>();
    private HashMap<String, Double> sd = new HashMap<>();

    public StandardScaler(boolean center, boolean scale) {
        this.center = center;
        this.scale = scale;
    }

    @Override
    public void train(Frame df) {

        varNames.clear();
        mean.clear();
        sd.clear();

        for (String varName : df.varNames()) {
            if (center) {
                varNames.add(varName);
                mean.put(varName, Mean.from(df.rvar(varName)).value());
            }
            if (scale) {
                varNames.add(varName);
                sd.put(varName, Variance.from(df.rvar(varName)).sdValue());
            }
        }
    }

    @Override
    public void transform(Frame df) {
        for (String varName : df.varNames()) {
            if (!varNames.contains(varName)) {
                continue;
            }
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, varName))
                    continue;
                double value = df.getDouble(i, varName);
                if (center) {
                    value -= mean.get(varName);
                }
                if (scale) {
                    value /= sd.get(varName);
                }
                df.setDouble(i, varName, value);
            }
        }
    }

    @Override
    public void reverse(Frame df) {
        for (String varName : df.varNames()) {
            if (!varNames.contains(varName)) {
                continue;
            }
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, varName))
                    continue;
                double value = df.getDouble(i, varName);
                if (scale) {
                    value *= sd.get(varName);
                }
                if (center) {
                    value += mean.get(varName);
                }
                df.setDouble(i, varName, value);
            }
        }
    }
}
