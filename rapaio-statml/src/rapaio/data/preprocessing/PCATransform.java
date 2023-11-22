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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.data.preprocessing;

import java.io.Serial;
import java.util.function.BiFunction;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.linear.DMatrix;
import rapaio.linear.DVector;
import rapaio.ml.analysis.PCA;

/**
 * Transforms a data frame
 */
public class PCATransform extends AbstractTransform {

    public static PCATransform featureCount(int max) {
        return featureCount("pca_", max, VarRange.all());
    }

    public static PCATransform featureCount(String prefix, int max) {
        return featureCount(prefix, max, VarRange.all());
    }

    public static PCATransform featureCount(int max, VarRange varRange) {
        return new PCATransform("pca_", (values, vectors) -> Math.min(values.size(), max), varRange);
    }

    public static PCATransform featureCount(String prefix, int max, VarRange varRange) {
        return new PCATransform(prefix, (values, vectors) -> Math.min(values.size(), max), varRange);
    }

    public static PCATransform coverVariance(double minPercentage) {
        return coverVariance("pca_", minPercentage, VarRange.all());
    }

    public static PCATransform coverVariance(String prefix, double minPercentage) {
        return coverVariance(prefix, minPercentage, VarRange.all());
    }

    public static PCATransform coverVariance(double minPercentage, VarRange varRange) {
        return coverVariance("pca_", minPercentage, varRange);
    }

    public static PCATransform coverVariance(String prefix, double minPercentage, VarRange varRange) {
        return new PCATransform(prefix, (values, vectors) -> {
            var coverage = values.copy().cumsum().div(values.sum());
            for (int i = 0; i < coverage.size(); i++) {
                if (coverage.get(i) >= minPercentage) {
                    return i + 1;
                }
            }
            return values.size();
        }, varRange);
    }

    @Serial
    private static final long serialVersionUID = 2797285371357486124L;

    final String prefix;
    final BiFunction<DVector, DMatrix, Integer> kFun;
    private PCA pca;

    private PCATransform(String prefix, BiFunction<DVector, DMatrix, Integer> kFun, VarRange varRange) {
        super(varRange);
        this.prefix = prefix;
        this.kFun = kFun;
    }

    @Override
    public PCATransform newInstance() {
        return new PCATransform(prefix, kFun, varRange);
    }

    @Override
    public void coreFit(Frame df) {
        pca = PCA.newModel();
        pca.fit(df.mapVars(varNames));
    }

    @Override
    public Frame coreApply(Frame df) {
        Frame rest = df.removeVars(VarRange.of(varNames));
        int k = kFun.apply(pca.getValues(), pca.getVectors());
        Frame trans = pca.transform(prefix, df.mapVars(varNames), k);
        return rest.varCount() == 0 ? trans : rest.bindVars(trans);
    }
}
