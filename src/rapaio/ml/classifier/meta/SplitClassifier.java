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

import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.data.stream.FSpot;
import rapaio.util.SPredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class SplitClassifier extends AbstractClassifier implements RunningClassifier {

    int runs = 0;
    Classifier c;
    List<SPredicate<FSpot>> predicates = new ArrayList<>();
    //
    List<Classifier> classifiers = new ArrayList<>();

    @Override
    public String name() {
        return "SplitClassifier";
    }

    @Override
    public String fullName() {
        return null;
    }

    @Override
    public SplitClassifier newInstance() {
        return new SplitClassifier();
    }

    public Classifier getClassifier() {
        return c;
    }

    public SplitClassifier withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    public List<SPredicate<FSpot>> getPredicates() {
        return predicates;
    }

    public SplitClassifier withPredicates(List<SPredicate<FSpot>> predicates) {
        this.predicates = predicates;
        return this;
    }

    @Override
    public SplitClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public void learn(Frame df, String targetColName) {
        dict = df.col(targetColName).getDictionary();
        targetCol = targetColName;

        if (c == null) {
            throw new IllegalArgumentException("classifier could not be null");
        }

        List<Mapping> maps = new ArrayList<>();
        for (int i = 0; i < predicates.size() + 1; i++) {
            maps.add(new Mapping());
        }
        df.stream().forEach(spot -> {
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).test(spot)) {
                    maps.get(i).add(spot.rowId());
                    return;
                }
            }
            maps.get(maps.size() - 1).add(spot.rowId());
        });
        List<Frame> frames = new ArrayList<>();
        for (Mapping map : maps) {
            frames.add(new MappedFrame(df.source(), map));
        }

        classifiers = new ArrayList<>();
        for (int i = 0; i < predicates.size() + 1; i++) {
            Classifier ni = c.newInstance();
            if(ni instanceof RunningClassifier) ((RunningClassifier)ni).withRuns(runs);
            classifiers.add(ni);
        }
        for (int i = 0; i < classifiers.size(); i++) {
            if (frames.get(i).rowCount() > 0)
                classifiers.get(i).learn(frames.get(i), targetColName);
        }
    }

    @Override
    public void learnFurther(Frame df, String targetName, int runs) {
        dict = df.col(targetName).getDictionary();
        targetCol = targetName;

        if (c == null) {
            throw new IllegalArgumentException("classifier could not be null");
        }
        if (!(c instanceof RunningClassifier)) {
            throw new IllegalArgumentException("classifier must be a running classifier");
        }

        List<Mapping> maps = new ArrayList<>();
        for (int i = 0; i < predicates.size() + 1; i++) {
            maps.add(new Mapping());
        }
        df.stream().forEach(spot -> {
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).test(spot)) {
                    maps.get(i).add(spot.rowId());
                    return;
                }
                maps.get(maps.size() - 1).add(spot.rowId());
            }
        });
        List<Frame> frames = new ArrayList<>();
        for (Mapping map : maps) {
            frames.add(new MappedFrame(df.source(), map));
        }

        for (int i = 0; i < classifiers.size(); i++) {
            if (frames.get(i).rowCount() > 0)
                ((RunningClassifier) classifiers.get(i)).learnFurther(frames.get(i), targetName, runs);
        }
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        df.stream().forEach(spot -> {
            for (int i = 0; i < predicates.size(); i++) {
                if (predicates.get(i).test(spot)) {
                    Frame f = new MappedFrame(df.source(), new Mapping(new int[]{spot.rowId()}));
                    classifiers.get(i).predict(f);
                    pred.setLabel(spot.row(), classifiers.get(i).pred().getLabel(0));
                    for (int j = 0; j < dict.length; j++) {
                        dist.setValue(spot.row(), dict[j], classifiers.get(i).dist().getValue(0, dict[j]));
                    }
                    return;
                }
            }
            Frame f = new MappedFrame(df.source(), new Mapping(new int[]{spot.rowId()}));
            classifiers.get(classifiers.size() - 1).predict(f);
            pred.setLabel(spot.row(), classifiers.get(classifiers.size() - 1).pred().getLabel(0));
            for (int j = 0; j < dict.length; j++) {
                dist.setValue(spot.row(), dict[j], classifiers.get(classifiers.size() - 1).dist().getValue(0, dict[j]));
            }
        });
    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }

    public static List<SPredicate<FSpot>> splitByNominal(Frame df, String colName) {
        List<SPredicate<FSpot>> list = new ArrayList<>();
        Arrays.stream(df.col(colName).getDictionary()).forEach(term ->{
            list.add(spot -> spot.getLabel(colName).equals(term));
        });
        return list;
    }
}
