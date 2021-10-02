/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.classifier.bayes.nb;

import java.io.Serial;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.printer.Format;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/18/15.
 */
public class KernelEstimator extends AbstractEstimator {

    public static KernelEstimator forName(String testName) {
        return forName(testName, new KFuncGaussian());
    }

    public static KernelEstimator forName(String testName, KFunc kfunc) {
        return forName(testName, kfunc, 0);
    }

    public static KernelEstimator forName(String testName, KFunc kfunc, double bandwidth) {
        return new KernelEstimator(testName, kfunc, bandwidth);
    }

    public static List<Estimator> forRange(Frame df, VarRange varRange) {
        List<String> varNames = varRange.parseVarNames(df);
        return varNames.stream()
                .map(name -> new KernelEstimator(name, new KFuncGaussian(), 0))
                .collect(Collectors.toList());
    }

    public static List<Estimator> forType(Frame df, VarType type) {
        return forType(df, type, new KFuncGaussian());
    }

    public static List<Estimator> forType(Frame df, VarType type, KFunc kfunc) {
        return forType(df, type, kfunc, 0);
    }

    public static List<Estimator> forType(Frame df, VarType type, KFunc kfunc, double bandwidth) {
        return VarRange.onlyTypes(type).parseVarNames(df).stream()
                .map(name -> new KernelEstimator(name, kfunc, bandwidth))
                .collect(Collectors.toList());
    }

    @Serial
    private static final long serialVersionUID = 7974390604811353859L;

    private final String testVarName;
    private final KFunc kfunc;
    private final double bandwidth;

    private Map<String, KDE> kdes = new ConcurrentHashMap<>();

    private KernelEstimator(String testVarName, KFunc kfunc, double bandwidth) {
        super(Collections.singletonList(testVarName));
        this.testVarName = testVarName;
        this.kfunc = kfunc;
        this.bandwidth = bandwidth;
    }

    @Override
    public Estimator newInstance() {
        return new KernelEstimator(testVarName, kfunc, bandwidth);
    }

    @Override
    public String name() {
        return "Kernel{test=" + testVarName + "}";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append("Kernel{test=").append(testVarName);
        sb.append(", kdes=[");
        sb.append(kdes.entrySet().stream()
                .map(e -> e.getKey() + ":{kfun=" + e.getValue().kernel().toString() + ",bw=" + Format.floatFlex(e.getValue().bandwidth()) + "}")
                .collect(Collectors.joining(",")));
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean fit(Frame df, Var weights, String targetName) {
        kdes = new ConcurrentHashMap<>();
        df.levels(targetName).parallelStream().forEach(
                targetLevel -> {
                    if ("?".equals(targetLevel)) {
                        return;
                    }
                    Frame cond = df.stream().filter(s -> targetLevel.equals(s.getLabel(targetName))).toMappedFrame();
                    Var v = cond.rvar(testVarName);
                    kdes.put(targetLevel, KDE.of(v, kfunc, bandwidth == 0 ? KDE.silvermanBandwidth(v) : bandwidth));
                });
        return true;
    }

    @Override
    public double predict(Frame df, int row, String targetLevel) {
        if (kdes.containsKey(targetLevel)) {
            return kdes.get(targetLevel).pdf(df.getDouble(row, testVarName));
        }
        return Double.NaN;
    }
}

