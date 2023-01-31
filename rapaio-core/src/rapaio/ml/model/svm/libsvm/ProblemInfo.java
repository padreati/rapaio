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

package rapaio.ml.model.svm.libsvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.mapping.ArrayMapping;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.model.svm.OneClassSvm;
import rapaio.ml.model.svm.SvmClassifier;
import rapaio.ml.model.svm.SvmRegression;

public record ProblemInfo(Random random, DVector[] xs, double[] y,
                          List<String> levels, Map<String, Integer> index, Map<String, Mapping> map,
                          SvmClassifier.Penalty cType, SvmRegression.Penalty rType, Kernel kernel, long cacheSize, double eps,
                          double c, Map<String, Double> weighting, double nu, double p, boolean shrinking, boolean probability) {

    public static ProblemInfo from(DMatrix x, Var target, SvmClassifier parent) {
        DVector[] xs = new DVector[x.rows()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = x.mapRow(i);
        }
        double[] y = new double[x.rows()];
        for (int i = 0; i < x.rows(); i++) {
            switch (target.type()) {
                case BINARY, INT -> y[i] = target.getInt(i);
                case NOMINAL -> y[i] = target.getInt(i) - 1;
                default -> throw new IllegalArgumentException("Not implemented");
            }
        }
        List<String> levels = new ArrayList<>();
        Map<String, Integer> index = new HashMap<>();
        Map<String, Mapping> map = new HashMap<>();

        if (parent.levels.get().isEmpty()) {
            // specified by the dictionary from target variable
            target.levels().stream().skip(1).forEach(label -> {
                index.put(label, levels.size());
                levels.add(label);
                map.put(label, new ArrayMapping());
            });
        } else {
            for (String level : parent.levels.get()) {
                index.put(level, levels.size());
                levels.add(level);
                map.put(level, new ArrayMapping());
            }
        }

        for (int i = 0; i < target.size(); i++) {
            String label = target.getLabel(i);
            // count only for specified levels
            if (index.containsKey(label)) {
                map.get(label).add(i);
            }
        }

        // default parameter values taken from classifier

        return new ProblemInfo(new Random(parent.seed.get()), xs, y, levels, index, map,
                parent.type.get(), null, parent.kernel.get(), parent.cacheSize.get(),
                parent.tolerance.get(), parent.c.get(), new HashMap<>(parent.wi.get()),
                parent.nu.get(), 0.0, parent.shrinking.get(), parent.probability.get());
    }

    public static ProblemInfo from(DMatrix x, Var target, SvmRegression parent) {
        DVector[] xs = new DVector[x.rows()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = x.mapRow(i);
        }
        double[] y = new double[x.rows()];
        for (int i = 0; i < x.rows(); i++) {
            switch (target.type()) {
                case BINARY, INT -> y[i] = target.getInt(i);
                case DOUBLE -> y[i] = target.getDouble(i);
                default -> throw new IllegalArgumentException("Not implemented");
            }
        }

        // default parameter values taken from classifier

        return new ProblemInfo(new Random(parent.seed.get()), xs, y, List.of(), Map.of(), Map.of(),
                null, parent.type.get(), parent.kernel.get(), parent.cacheSize.get(),
                parent.tolerance.get(), parent.c.get(), Map.of(),
                parent.nu.get(), parent.epsilon.get(), parent.shrinking.get(), parent.probability.get());
    }

    public static ProblemInfo from(DMatrix x, Var target, OneClassSvm parent) {
        DVector[] xs = new DVector[x.rows()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = x.mapRow(i);
        }
        double[] y = new double[x.rows()];
        for (int i = 0; i < x.rows(); i++) {
            switch (target.type()) {
                case BINARY, INT -> y[i] = target.getInt(i);
                case DOUBLE -> y[i] = target.getDouble(i);
                default -> throw new IllegalArgumentException("Not implemented");
            }
        }

        // default parameter values taken from classifier

        return new ProblemInfo(new Random(parent.seed.get()), xs, y, List.of(), Map.of(), Map.of(),
                null, null, parent.kernel.get(), parent.cacheSize.get(),
                parent.tolerance.get(), 0.0, Map.of(),
                parent.nu.get(), 0.0, parent.shrinking.get(), false);
    }

    public SvmParameter computeParameters() {
        SvmParameter param = new SvmParameter();
        if (cType != null) {
            param.svmType = cType == SvmClassifier.Penalty.C ? 0 : 1;
        } else if (rType != null) {
            param.svmType = rType == SvmRegression.Penalty.C ? 3 : 4;
        } else {
            param.svmType = 2;
        }
        param.kernel = kernel;

        param.cacheSize = cacheSize;
        param.eps = eps;
        param.c = c;    // for C_SVC, EPSILON_SVR and NU_SVR

        param.nrWeight = weighting.size();        // for C_SVC
        param.weightLabel = new int[param.nrWeight];    // for C_SVC
        param.weight = new double[param.nrWeight];        // for C_SVC
        int pos = 0;
        for (var w : weighting.entrySet()) {
            if (index.containsKey(w.getKey())) {
                param.weightLabel[pos] = index.get(w.getKey());
                param.weight[pos] = w.getValue();
                pos++;
            }
        }

        if (param.nrWeight == 2) {
            param.weightLabel[0] = 1;
            param.weightLabel[1] = -1;
        }

        param.nu = nu;    // for NU_SVC, ONE_CLASS, and NU_SVR
        param.p = p;    // for EPSILON_SVR
        param.shrinking = shrinking ? 1 : 0;    // use the shrinking heuristics
        param.probability = probability ? 1 : 0; // do probability estimates

        return param;
    }

    public SvmProblem computeProblem() {
        SvmProblem prob = new SvmProblem();
        prob.random = random;
        prob.len = xs.length;
        prob.xs = xs;
        prob.y = y;
        return prob;
    }

    public int size() {
        return xs.length;
    }

    public Map<String, Mapping> getMappings() {
        return map;
    }

    public boolean checkValidProblem() {

        // nu-svc check
//        if (type == SVMClassifier.Penalty.NU) {
//            for (int i = 0; i < levels.size(); i++) {
//                int ni = map.get(levels.get(i)).size();
//                for (int j = i + 1; j < levels.size(); j++) {
//                    int nj = map.get(levels.get(j)).size();
//                    if (nu * (ni + nj) / 2 > Math.min(ni, nj)) {
//                        throw new IllegalArgumentException("NU_SVC problem is not feasible.");
//                    }
//                }
//            }
//        }

        return true;
    }
}
