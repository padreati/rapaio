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

package rapaio.experiment.ml.classifier.bayes;

import rapaio.core.tools.*;
import rapaio.data.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public interface PriorSupplier extends Serializable {

    String name();

    Map<String, Double> learnPriors(Frame df, Var weights, NaiveBayes nb);

    PriorSupplier PRIOR_MLE = new PriorSupplier() {
        private static final long serialVersionUID = 2590209274166763951L;

        @Override
        public String name() {
            return "PRIOR_MLE";
        }

        @Override
        public Map<String, Double> learnPriors(Frame df, Var weights, NaiveBayes nb) {
            Map<String, Double> priors = new HashMap<>();
            DVector dv = DVector.fromWeights(false, df.rvar(nb.firstTargetName()), weights);
            dv.normalize();
            for (int i = 1; i < nb.firstTargetLevels().size(); i++) {
                priors.put(nb.firstTargetLevels().get(i), dv.get(i));
            }
            return priors;
        }
    };

    PriorSupplier PRIOR_UNIFORM = new PriorSupplier() {
        private static final long serialVersionUID = 4346918440758937122L;

        @Override
        public String name() {
            return "PRIOR_UNIFORM";
        }

        @Override
        public Map<String, Double> learnPriors(Frame df, Var weights, NaiveBayes nb) {
            Map<String, Double> priors = new HashMap<>();
            double p = 1.0 / nb.firstTargetLevels().size();
            for (int i = 1; i < nb.firstTargetLevels().size(); i++) {
                priors.put(nb.firstTargetLevels().get(i), p);
            }
            return priors;
        }
    };
}
