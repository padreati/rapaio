/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.classifier.ensemble;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.ClassifierResult;

import java.io.Serializable;
import java.util.List;

/**
 * Describes and implements how a class is obtained from ensemble results.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
@RequiredArgsConstructor
@Getter
public enum BaggingMode implements Serializable {

    VOTING(true, false) {
        @Override
        public void computeDensity(List<String> dictionary, List<ClassifierResult> predictions, VarNominal classes, Frame densities) {
            predictions.stream().map(ClassifierResult::firstClasses).forEach(d -> {
                for (int i = 0; i < d.size(); i++) {
                    int best = d.getInt(i);
                    densities.setDouble(i, best, densities.getDouble(i, best) + 1);
                }
            });
            for (int i = 0; i < classes.size(); i++) {
                var dv = DensityVector.emptyByLabels(false, dictionary);
                for (int j = 1; j < dictionary.size(); j++) {
                    dv.increment(dictionary.get(j), densities.getDouble(i, j));
                }
                dv.normalize();
                for (int j = 1; j < dictionary.size(); j++) {
                    densities.setDouble(i, j, dv.get(dictionary.get(j)));
                }
                classes.setDouble(i, dv.findBestIndex() + 1);
            }
        }
    },
    DISTRIBUTION(false, true) {
        @Override
        public void computeDensity(List<String> dictionary, List<ClassifierResult> results, VarNominal classes, Frame densities) {
            for (int i = 0; i < densities.rowCount(); i++) {
                for (int j = 0; j < densities.varCount(); j++) {
                    densities.setDouble(i, j, 0);
                }
            }
            results.stream().map(ClassifierResult::firstDensity).forEach(d -> {
                for (int i = 0; i < densities.rowCount(); i++) {
                    double t = 0.0;
                    for (int j = 0; j < densities.varCount(); j++) {
                        t += d.getDouble(i, j);
                    }
                    for (int j = 0; j < densities.varCount(); j++) {
                        densities.setDouble(i, j, densities.getDouble(i, j) + d.getDouble(i, j) / t);
                    }
                }
            });
            for (int i = 0; i < classes.size(); i++) {
                var dv = DensityVector.emptyByLabels(false, dictionary);
                for (int j = 1; j < dictionary.size(); j++) {
                    dv.increment(dictionary.get(j), densities.getDouble(i, j));
                }
                dv.normalize();
                for (int j = 1; j < dictionary.size(); j++) {
                    densities.setDouble(i, j, dv.get(dictionary.get(j)));
                }
                classes.setDouble(i, dv.findBestIndex() + 1);
            }
        }
    };

    private final boolean useClass;
    private final boolean useDensities;

    abstract void computeDensity(List<String> dictionary, List<ClassifierResult> treeFits, VarNominal classes, Frame densities);
}
