/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.regressor.tree.rtree;

import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public interface RTreeNominalMethod {

    String name();

    List<RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function);

    public RTreeNominalMethod IGNORE = new RTreeNominalMethod() {

        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public List<RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function) {
            return new ArrayList<>();
        }
    };

//    public RTreeNominalMethod FULL = new RTreeNominalMethod() {
//
//        @Override
//        public String name() {
//            return "FULL";
//        }
//
//        @Override
//        public List<RTreeCandidate> computeCandidates(RTree c, Frame df, Numeric weights, String testColName, String targetColName, RTreeTestFunction function) {
//            List<RTreeCandidate> result = new ArrayList<>();
//            Var test = df.var(testColName);
//            Var target = df.var(targetColName);
//
//            if (new DensityTable(test, target).countWithMinimum(false, c.getMinCount()) < 2) {
//                return result;
//            }
//
//            DensityTable dt = new DensityTable(test, target, weights);
//            double value = function.compute(dt);
//
//            CTreeCandidate candidate = new CTreeCandidate(value, function.sign(), testColName);
//            for (int i = 1; i < test.dictionary().length; i++) {
//
//                final String label = test.dictionary()[i];
//                candidate.addGroup(
//                        String.format("%s == %s", testColName, label),
//                        spot -> !spot.missing(testColName) && spot.label(testColName).equals(label));
//            }
//
//            result.add(candidate);
//            return result;
//        }
//    };
//
//    public CTreeNominalMethod BINARY = new CTreeNominalMethod() {
//
//        @Override
//        public String name() {
//            return "BINARY";
//        }
//
//
//        @Override
//        public List<CTreeCandidate> computeCandidates(CTree c, Frame df, Numeric weights, String testColName, String targetColName, CTreeTestFunction function) {
//
//            List<CTreeCandidate> result = new ArrayList<>();
//            CTreeCandidate best = null;
//            for (int i = 1; i < df.var(testColName).dictionary().length; i++) {
//                Var test = df.var(testColName);
//                Var target = df.var(targetColName);
//                String testLabel = df.var(testColName).dictionary()[i];
//
//                if (new DensityTable(test, target).countWithMinimum(false, c.getMinCount()) < 2) {
//                    return result;
//                }
//
//                DensityTable dt = new DensityTable(test, target, weights, testLabel);
//                double value = function.compute(dt);
//                CTreeCandidate candidate = new CTreeCandidate(value, function.sign(), testColName);
//                if (best == null) {
//                    best = candidate;
//                    best.addGroup(testColName + " == " + testLabel, spot -> spot.label(testColName).equals(testLabel));
//                    best.addGroup(testColName + " != " + testLabel, spot -> !spot.label(testColName).equals(testLabel));
//                } else {
//                    int comp = best.compareTo(candidate);
//                    if (comp < 0) continue;
//                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
//                    best = candidate;
//                    best.addGroup(testColName + " == " + testLabel, spot -> spot.label(testColName).equals(testLabel));
//                    best.addGroup(testColName + " != " + testLabel, spot -> !spot.label(testColName).equals(testLabel));
//                }
//            }
//            if (best != null)
//                result.add(best);
//            return result;
//        }
//    };
}