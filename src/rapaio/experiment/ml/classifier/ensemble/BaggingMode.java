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

package rapaio.experiment.ml.classifier.ensemble;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.ClassifierResult;

import java.io.Serializable;
import java.util.List;

/**
 * Describes and implements how a class is obtained from density for ensemble methods.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public enum BaggingMode implements Serializable {

    VOTING {
        @Override
        public void computeDensity(List<String> dictionary, List<ClassifierResult> treeFits, VarNominal classes, Frame densities) {
            treeFits.stream().map(ClassifierResult::firstClasses).forEach(d -> {
                for (int i = 0; i < d.rowCount(); i++) {
                    int best = d.getInt(i);
                    densities.setDouble(i, best, densities.getDouble(i, best) + 1);
                }
            });
            for (int i = 0; i < classes.rowCount(); i++) {
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

        @Override
        boolean needsClass() {
            return true;
        }

        @Override
        boolean needsDensity() {
            return false;
        }
    },
    DISTRIBUTION {
        @Override
        public void computeDensity(List<String> dictionary, List<ClassifierResult> treeFits, VarNominal classes, Frame densities) {
            for (int i = 0; i < densities.rowCount(); i++) {
                for (int j = 0; j < densities.varCount(); j++) {
                    densities.setDouble(i, j, 0);
                }
            }
            treeFits.stream().map(ClassifierResult::firstDensity).forEach(d -> {
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
            for (int i = 0; i < classes.rowCount(); i++) {
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

        @Override
        boolean needsClass() {
            return false;
        }

        @Override
        boolean needsDensity() {
            return true;
        }
    };

    abstract void computeDensity(List<String> dictionary, List<ClassifierResult> treeFits, VarNominal classes, Frame densities);

    abstract boolean needsClass();

    abstract boolean needsDensity();
}
