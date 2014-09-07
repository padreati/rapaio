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

package rapaio.ml.classifier.meta;

import rapaio.data.*;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.data.stream.FSpot;
import rapaio.util.SPredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
//@Deprecated
//public class SplitClassifier extends AbstractClassifier implements RunningClassifier {
//
//    int runs = 0;
//    Classifier c;
//    List<SPredicate<FSpot>> predicates = new ArrayList<>();
//    //
//    List<Classifier> classifiers = new ArrayList<>();
//
//    @Override
//    public String name() {
//        return "SplitClassifier";
//    }
//
//    @Override
//    public String fullName() {
//        return null;
//    }
//
//    @Override
//    public SplitClassifier newInstance() {
//        return new SplitClassifier();
//    }
//
//    public Classifier getClassifier() {
//        return c;
//    }
//
//    public SplitClassifier withClassifier(Classifier c) {
//        this.c = c;
//        return this;
//    }
//
//    public List<SPredicate<FSpot>> getPredicates() {
//        return predicates;
//    }
//
//    public SplitClassifier withPredicates(List<SPredicate<FSpot>> predicates) {
//        this.predicates = predicates;
//        return this;
//    }
//
//    @Override
//    public SplitClassifier withRuns(int runs) {
//        this.runs = runs;
//        return this;
//    }
//
//    @Override
//    public void learn(Frame df, Numeric weights, String targetVars) {
//        dict = df.var(targetVars).dictionary();
//        this.targetVars = targetVars;
//
//        if (c == null) {
//            throw new IllegalArgumentException("classifier could not be null");
//        }
//
//        List<Mapping> maps = new ArrayList<>();
//        for (int i = 0; i < predicates.size() + 1; i++) {
//            maps.add(Mapping.newEmpty());
//        }
//        df.stream().forEach(spot -> {
//            for (int i = 0; i < predicates.size(); i++) {
//                if (predicates.get(i).test(spot)) {
//                    maps.get(i).add(spot.row());
//                    return;
//                }
//            }
//            maps.get(maps.size() - 1).add(spot.row());
//        });
//        List<Frame> frames = new ArrayList<>();
//        for (Mapping map : maps) {
//            frames.add(MappedFrame.newByRow(df, map));
//        }
//
//        classifiers = new ArrayList<>();
//        for (int i = 0; i < predicates.size() + 1; i++) {
//            Classifier ni = c.newInstance();
//            if(ni instanceof RunningClassifier) ((RunningClassifier)ni).withRuns(runs);
//            classifiers.add(ni);
//        }
//        for (int i = 0; i < classifiers.size(); i++) {
//            if (frames.get(i).rowCount() > 0)
//                classifiers.get(i).learn(frames.get(i), targetVars);
//        }
//    }
//
//    @Override
//    public void learnFurther(Frame df, Numeric weights, String targetVars, int runs) {
//        dict = df.var(targetVars).dictionary();
//        this.targetVars = targetVars;
//
//        if (c == null) {
//            throw new IllegalArgumentException("classifier could not be null");
//        }
//        if (!(c instanceof RunningClassifier)) {
//            throw new IllegalArgumentException("classifier must be a running classifier");
//        }
//
//        List<Mapping> maps = new ArrayList<>();
//        List<Numeric> w = new ArrayList<>();
//        for (int i = 0; i < predicates.size() + 1; i++) {
//            maps.add(Mapping.newEmpty());
//            w.add(Numeric.newEmpty());
//        }
//        df.stream().forEach(spot -> {
//            for (int i = 0; i < predicates.size(); i++) {
//                if (predicates.get(i).test(spot)) {
//                    maps.get(i).add(spot.row());
//                    w.get(i).addValue(weights.value(spot.row()));
//                    return;
//                }
//                maps.get(maps.size() - 1).add(spot.row());
//                w.get(maps.size()-1).addValue(weights.value(spot.row()));
//            }
//        });
//        List<Frame> frames = new ArrayList<>();
//        for (Mapping map : maps) {
//            frames.add(MappedFrame.newByRow(df, map));
//        }
//
//        for (int i = 0; i < classifiers.size(); i++) {
//            if (frames.get(i).rowCount() > 0)
//                ((RunningClassifier) classifiers.get(i)).learnFurther(frames.get(i), w.get(i), targetVars, runs);
//        }
//    }
//
//    @Override
//    public void predict(Frame df) {
//        classes = Nominal.newEmpty(df.rowCount(), dict);
//        densities = SolidFrame.newMatrix(df.rowCount(), dict);
//
//        df.stream().forEach(spot -> {
//            for (int i = 0; i < predicates.size(); i++) {
//                if (predicates.get(i).test(spot)) {
//                    Frame f = MappedFrame.newByRow(df, spot.row());
//                    classifiers.get(i).predict(f);
//                    classes.setLabel(spot.row(), classifiers.get(i).classes().label(0));
//                    for (int j = 0; j < dict.length; j++) {
//                        densities.setValue(spot.row(), dict[j], classifiers.get(i).densities().value(0, dict[j]));
//                    }
//                    return;
//                }
//            }
//            Frame f = MappedFrame.newByRow(df, spot.row());
//            classifiers.get(classifiers.size() - 1).predict(f);
//            classes.setLabel(spot.row(), classifiers.get(classifiers.size() - 1).classes().label(0));
//            for (int j = 0; j < dict.length; j++) {
//                densities.setValue(spot.row(), dict[j], classifiers.get(classifiers.size() - 1).densities().value(0, dict[j]));
//            }
//        });
//    }
//
//    @Override
//    public void buildSummary(StringBuilder sb) {
//
//    }
//
//    public static List<SPredicate<FSpot>> splitByNominal(Frame df, String colName) {
//        List<SPredicate<FSpot>> list = new ArrayList<>();
//        Arrays.stream(df.var(colName).dictionary()).forEach(term ->{
//            list.add(spot -> spot.label(colName).equals(term));
//        });
//        return list;
//    }
//}
