/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier.meta;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.common.Capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
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

    @Override
    public Capabilities capabilities() {
        throw new IllegalArgumentException("not implemented yet");
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
    public SplitClassifier learn(Frame df, Var weights, String... targetVarNames) {
        prepareLearning(df, weights, targetVarNames);
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
            split.classifier.learn(frames.get(i), weightList.get(i), targetNames());
        }
        return this;
    }

    @Override
    public void learnFurther(Frame df, Var weights, String targetVars, int runs) {
        withRuns(runs);
        learn(df, weights, targetVars);
    }

    @Override
    public CFit fit(Frame df, boolean withClasses, boolean withDensities) {

        CFit pred = CFit.newEmpty(this, df, withClasses, withDensities);
        for (String targetVar : targetNames()) {
            pred.addTarget(targetVar, dictionaries().get(targetVar));
        }

        df.stream().forEach(spot -> {
            for (Split split : splits) {
                if (split.predicate.test(spot)) {

                    Frame f = MappedFrame.newByRow(df, spot.row());
                    CFit p = split.classifier.fit(f, withClasses, withDensities);

                    if (withClasses) {
                        for (String targetVar : targetNames()) {
                            pred.classes(targetVar).setLabel(spot.row(), p.classes(targetVar).label(0));
                        }
                    }
                    if (withDensities) {
                        for (String targetVar : targetNames()) {
                            for (int j = 0; j < dictionary(targetVar).length; j++) {
                                pred.densities().get(targetVar).setValue(spot.row(), dictionary(targetVar)[j], p.densities().get(targetVar).value(0, dictionary(targetVar)[j]));
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
    public CFit fitFurther(CFit fit, Frame df) {
        throw new IllegalArgumentException("not implemented yet");
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
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
