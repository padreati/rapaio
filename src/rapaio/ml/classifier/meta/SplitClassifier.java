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
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class SplitClassifier extends AbstractClassifier implements RunningClassifier {

    boolean ignoreUncovered = true;
    List<Split> splits = new ArrayList<>();
    //
    int runs = 1;

    @Override
    public String name() {
        return "SplitClassifier";
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public SplitClassifier newInstance() {
        return new SplitClassifier()
                .withRuns(runs)
                .withIgnoreUncovered(ignoreUncovered)
                .withSplits(splits);
    }

    public SplitClassifier withSplit(Predicate<FSpot> predicate, Classifier c) {
        this.splits.add(new Split(predicate, c));
        return this;
    }

    public SplitClassifier withSplits(List<Split> splits) {
        this.splits = new ArrayList<>(splits);
        return this;
    }

    public SplitClassifier withIgnoreUncovered(boolean ignoreUncovered) {
        this.ignoreUncovered = ignoreUncovered;
        return this;
    }

    @Override
    public SplitClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        List<String> list = new VarRange(targetVarNames).parseVarNames(df);
        this.targetVars = list.toArray(new String[list.size()]);
        this.dict = new HashMap<>();
        for (String targetVar : targetVars) {
            dict.put(targetVar, df.var(targetVar).dictionary());
        }

        if (splits.isEmpty()) {
            throw new IllegalArgumentException("No splits defined");
        }
        List<Mapping> maps = new ArrayList<>();
        for (int i = 0; i < splits.size(); i++) {
            maps.add(Mapping.newEmpty());
        }
        Mapping ignored = Mapping.newEmpty();
        df.stream().forEach(s -> {
            for (int i = 0; i < splits.size(); i++) {
                if (splits.get(i).predicate.test(s)) {
                    maps.get(i).add(s.row());
                    return;
                }
            }
            ignored.add(s.row());
        });

        // if we do not allow ignore uncovered values, than throw an error

        if (!ignoreUncovered && ignored.size() > 0) {
            throw new IllegalArgumentException("there are uncovered cases by splits, learning failed");
        }

        List<Frame> frames = maps.stream().map(df::mapRows).collect(Collectors.toList());
        List<Var> weightList = maps.stream().map(weights::mapRows).collect(Collectors.toList());

        for (int i = 0; i < splits.size(); i++) {
            Split split = splits.get(i);

            if (split.classifier instanceof RunningClassifier) {
                ((RunningClassifier) split.classifier).withRuns(runs);
            }
            split.classifier.learn(frames.get(i), weightList.get(i), targetVars);
        }
    }

    @Override
    public void learnFurther(Frame df, Var weights, String targetVars, int runs) {
        withRuns(runs);
        learn(df, weights, targetVars);
    }

    @Override
    public CPrediction predict(Frame df, boolean withClasses, boolean withDensities) {

        CPrediction pred = CPrediction.newEmpty(df.rowCount(), withClasses, withDensities);
        for (String targetVar : targetVars) {
            pred.addTarget(targetVar, dict.get(targetVar));
        }

        df.stream().forEach(spot -> {
            for (Split split : splits) {
                if (split.predicate.test(spot)) {

                    Frame f = MappedFrame.newByRow(df, spot.row());
                    CPrediction p = split.classifier.predict(f, withClasses, withDensities);

                    if (withClasses) {
                        for (String targetVar : targetVars) {
                            pred.classes(targetVar).setLabel(spot.row(), p.classes(targetVar).label(0));
                        }
                    }
                    if (withDensities) {
                        for (String targetVar : targetVars) {
                            for (int j = 0; j < dict.get(targetVar).length; j++) {
                                pred.densities().get(targetVar).setValue(spot.row(), dict.get(targetVar)[j], p.densities().get(targetVar).value(0, dict.get(targetVar)[j]));
                            }
                        }
                    }
                    return;
                }
            }
        });
        return pred;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("NotImplementedYet");
    }

    public static class Split {

        private final Predicate<FSpot> predicate;
        private final Classifier classifier;

        public Split(Predicate<FSpot> predicate, Classifier classifier) {
            this.predicate = predicate;
            this.classifier = classifier;
        }
    }
}
