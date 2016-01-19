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

package rapaio.experiment.classifier.meta;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.printer.Printable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class SplitClassifier extends AbstractClassifier implements Printable {

    private static final long serialVersionUID = 3332377951136731541L;

    boolean ignoreUncovered = true;
    List<Split> splits = new ArrayList<>();

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
        return (SplitClassifier) new SplitClassifier()
                .withIgnoreUncovered(ignoreUncovered)
                .withSplits(splits)
                .withRuns(runs());
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
    public boolean coreTrain(Frame df, Var weights) {
        if (splits.isEmpty()) {
            throw new IllegalArgumentException("No splits defined");
        }

        List<Mapping> maps = new ArrayList<>();
        for (int i = 0; i < splits.size(); i++) {
            maps.add(Mapping.empty());
        }
        Mapping ignored = Mapping.empty();
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
            split.classifier.withRuns(runs());
            split.classifier.train(frames.get(i), weightList.get(i), targetNames());
        }
        return true;
    }

    @Override
    public CFit coreFit(Frame df, boolean withClasses, boolean withDensities) {

        CFit pred = CFit.build(this, df, withClasses, withDensities);
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
                            for (int j = 0; j < targetLevels(targetVar).length; j++) {
                                pred.densities().get(targetVar).setValue(spot.row(), targetLevels(targetVar)[j], p.densities().get(targetVar).value(0, targetLevels(targetVar)[j]));
                            }
                        }
                    }
                    return;
                }
            }
        });
        return pred;
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
