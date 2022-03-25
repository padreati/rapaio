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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

class svm_predict {

    static void info(String s) {
        System.out.println(s);
    }

    public static record Prediction(double[] classes, DMatrix density) {
    }

    public static Prediction predict(svm_model model, DMatrix xs, int predict_probability) throws IOException {

        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;

        if (predict_probability == 1) {
            if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
                svm_predict.info(
                        "Prob. model for test data: target value = predicted value + z,\n"
                                + "z: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
                                + svm.svm_get_svr_probability(model) + "\n");
            } else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
            }
        }

        double[] classes = new double[xs.rows()];
        DMatrix density = DMatrix.empty(xs.rows(), nr_class);

        for (int i = 0; i < xs.rows(); i++) {

            DVector row = xs.mapRow(i);
            int m = row.size();
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = j;
                x[j].value = row.get(j);
            }

            double predict_label;
            if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
                predict_label = svm.svm_predict_probability(model, x, prob_estimates);
                classes[i] = predict_label;
                for (int j = 0; j < nr_class; j++) {
                    density.set(i, j, prob_estimates[j]);
                }
            } else {
                predict_label = svm.svm_predict(model, x);
                classes[i] = predict_label;
            }
        }
        return new Prediction(classes, density);
    }

    private static void exit_with_help() {
        System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
                + "options:\n"
                + "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
                + "-q : quiet mode (no outputs)\n");
        System.exit(1);
    }
}