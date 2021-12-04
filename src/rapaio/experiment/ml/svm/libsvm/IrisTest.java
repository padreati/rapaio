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
import rapaio.experiment.ml.svm.SVMClassifier;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.ClassifierResult;

public class IrisTest {

    /*
    "-s svm_type : set type of SVM (default 0)\n"
            + "	0 -- C-SVC		(multi-class classification)\n"
            + "	1 -- nu-SVC		(multi-class classification)\n"
            + "	2 -- one-class SVM\n"
            + "	3 -- epsilon-SVR	(regression)\n"
            + "	4 -- nu-SVR		(regression)\n"
            + "-t kernel_type : set type of kernel function (default 2)\n"
            + "	0 -- linear: u'*v\n"
            + "	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
            + "	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
            + "	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
            + "	4 -- precomputed kernel (kernel values in training_set_file)\n"
            + "-d degree : set degree in kernel function (default 3)\n"
            + "-g gamma : set gamma in kernel function (default 1/num_features)\n"
            + "-r coef0 : set coef0 in kernel function (default 0)\n"
            + "-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
            + "-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
            + "-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
            + "-m cachesize : set cache memory size in MB (default 100)\n"
            + "-e epsilon : set tolerance of termination criterion (default 0.001)\n"
            + "-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
            + "-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
            + "-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
            + "-v n : n-fold cross validation mode\n"
            + "-q : quiet mode (no outputs)\n"
     */
    public static void main(String[] args) throws IOException {
        Frame iris = Datasets.loadIrisDataset();

        DMatrix xs = DMatrix.copy(iris.mapVars(VarRange.onlyTypes(VarType.DOUBLE)));
        DVector ys = DVector.zeros(xs.rowCount());
        Var target = iris.rvar(4);
        for (int i = 0; i < xs.rowCount(); i++) {
            ys.set(i, target.getInt(i) - 1);
        }
        svm_train t = new svm_train();
        boolean prob = true;
        String[] argv = new String[] {"-s", "0", "-t", "2", "-g", "0.7", "-c", "10", "-b", prob ? "1" : "0"};
        svm_model model = t.run(xs, ys, argv);


        svm_predict.Prediction pred = svm_predict.predict(model, xs, prob ? 1 : 0);

        System.out.println(pred);

        SVMClassifier c = new SVMClassifier()
                .type.set(SVMClassifier.SvmType.C_SVC)
                .c.set(10.0)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.7));

        ClassifierResult cpred = c.fit(iris, "class").predict(iris);
        DMatrix cdensity = DMatrix.copy(cpred.firstDensity());

        pred.density().printContent();
        cdensity = cdensity.removeCols(new int[]{0});
        cdensity.printContent();

        System.out.println("Densities are the same: " + pred.density().deepEquals(cdensity, 1e-12));
    }
}
