/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import java.io.Serial;
import java.util.function.BiFunction;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.analysis.PCA;

/**
 * Transforms a data frame
 */
public class PCATransform extends AbstractTransform {

    public static PCATransform featureCount(int max) {
        return featureCount(max, VarRange.all());
    }

    public static PCATransform featureCount(int max, VarRange varRange) {
        return new PCATransform((values, vectors) -> Math.min(values.size(), max), varRange);
    }

    public static PCATransform coverVariance(double minPercentage) {
        return coverVariance(minPercentage, VarRange.all());
    }

    public static PCATransform coverVariance(double minPercentage, VarRange varRange) {
        return new PCATransform((values, vectors) -> {
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

    final BiFunction<DVector, DMatrix, Integer> kFun;
    private PCA pca;

    private PCATransform(BiFunction<DVector, DMatrix, Integer> kFun, VarRange varRange) {
        super(varRange);
        this.kFun = kFun;
    }

    @Override
    public PCATransform newInstance() {
        return new PCATransform(kFun, varRange);
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
        Frame trans = pca.transform(df.mapVars(varNames), k);
        return rest.varCount() == 0 ? trans : rest.bindVars(trans);
    }
}
