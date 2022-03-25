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

package rapaio.experiment.ml.svm.libsvm;

import java.io.IOException;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.svm.SVMClassifier;

public class IrisTest {

    public static void main(String[] args) throws IOException {
        Frame iris = Datasets.loadIrisDataset();

        DMatrix xs = DMatrix.copy(iris.mapVars(VarRange.onlyTypes(VarType.DOUBLE)));
        DVector ys = DVector.zeros(xs.rows());
        Var target = iris.rvar(4);
        for (int i = 0; i < xs.rows(); i++) {
            ys.set(i, target.getInt(i) - 1);
        }

        // classification, probability

        // libsvm

        svm_train t = new svm_train();
        boolean prob = true;
        String[] argv = new String[] {"-s", "0", "-t", "2", "-g", "0.7", "-c", "10", "-b", prob ? "1" : "0"};
        svm_model model = t.run(xs, ys, argv);
        svm_predict.Prediction pred = svm_predict.predict(model, xs, prob ? 1 : 0);

        // rapaio

        SVMClassifier c = new SVMClassifier()
                .type.set(SVMClassifier.Penalty.C)
                .c.set(10.0)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.7));

        ClassifierResult cpred = c.fit(iris, "class").predict(iris);
        DMatrix cdensity = DMatrix.copy(cpred.firstDensity()).removeCols(new int[] {0});

        System.out.println("Densities are the same: " + pred.density().deepEquals(cdensity, 1e-16));

        if (!pred.density().deepEquals(cdensity, 1e-16)) {
            pred.density().printContent();
            cdensity.printContent();
        }

    }
}
