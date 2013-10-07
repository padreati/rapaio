/*
 * Copyright 2013 Aurelian Tutuianu
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

package titanic;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.distributions.empirical.*;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import static rapaio.filters.BaseFilters.renameVector;
import rapaio.filters.ColFilters;
import static rapaio.filters.ColFilters.removeCols;
import static rapaio.filters.NominalFilters.combine;
import static rapaio.filters.NominalFilters.consolidate;
import static rapaio.filters.NumericFilters.imputeMissing;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.HistogramBars;
import rapaio.io.CsvPersistence;
import rapaio.ml.supervised.Classifier;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierProvider;
import rapaio.ml.supervised.CrossValidation;
import rapaio.ml.supervised.meta.Bagging;
import rapaio.ml.supervised.tree.ID3;
import rapaio.ml.supervised.tree.RandomForest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConsoleExplore {

    public static void main(String[] args) throws IOException {
        Frame train = Utils.read("train.csv");
        Frame test = Utils.read("test.csv");
        List<Frame> frames = consolidate(Arrays.asList(train, test));
        for (Frame df : frames) {
            imputeMissing(df, "Age", "mean",
                    df.getCol("Title"),
                    df.getCol("Pclass"),
                    df.getCol("Sex"),
                    df.getCol("Embarked")//,
//                    df.getCol("Parch"),
//                    df.getCol("SibSp")
            );
        }
//        frames = relate("TicketGroup", frames, "Ticket");
//        frames = relate("Group", frames, "Cabin", "Family", "Ticket", "SibSp");

//        List<String> combinations = new ArrayList<>();
//        combinations.add("Sex");
//        combinations.add("Pclass");
////        combinations.add("SibSp");
////        combinations.add("Parch");
//        combinations.add("Title");
//        combinations.add("Embarked");
////        combinations.add("Family");
//
//        for (int i = 0; i < combinations.size(); i++) {
//            for (int j = i+1; j < combinations.size(); j++) {
//                frames = combine(combinations.get(i)+combinations.get(j), frames, combinations.get(i), combinations.get(j));
//            }
//        }

        train = frames.get(0);
        test = frames.get(1);

        Frame tr = train;

        tr = removeCols(tr, "PassengerId");
        tr = removeCols(tr, "Name");
        tr = removeCols(tr, "Ticket");
        tr = removeCols(tr, "Cabin");
//        tr = removeCols(tr, "Age");
//        tr = removeCols(tr, "Fare");
//        tr = removeCols(tr, "SibSp");
        tr = removeCols(tr, "Parch");
//        tr = removeCols(tr, "Pclass");
//        tr = removeCols(tr, "Title");
//        tr = removeCols(tr, "Sex");
//        tr = removeCols(tr, "Embarked");
        tr = removeCols(tr, "ParchSib");
        tr = removeCols(tr, "Family");

        Summary.summary(tr);

        final int mtree = 500;
        final int mcols = 3;

        RandomForest rf = new RandomForest(mtree, mcols, RandomForest.IMPURITY_GINI, true);
        rf.setDebug(true);

        long start2 = System.currentTimeMillis();
        rf.learn(tr, "Survived");
        long end2 = System.currentTimeMillis();
        System.out.println("submit train took " + (end2 - start2) + " millis");
        System.out.flush();

        ClassifierModel cr = rf.predict(test);


        Frame submit = new SolidFrame("submit", test.getRowCount(), new Vector[]{
                test.getCol("PassengerId"),
                renameVector(cr.getClassification(), "Survived")
        });
        CsvPersistence persist = new CsvPersistence();
        persist.setColSeparator(',');
        persist.setHasQuotas(true);
        persist.setHasHeader(true);
        persist.write(submit, "/home/ati/work/rapaio/RapaioKaggle/src/titanic/submit.csv");

        closePrinter();
    }

    private static List<Frame> relate(String relationName, List<Frame> frames, String... colNames) {

        List<Frame> result = new ArrayList<>();

        int len = 0;
        for (Frame f : frames) {
            len += f.getRowCount();
        }
        int[] groups = new int[len];
        for (int i = 0; i < len; i++) {
            groups[i] = i;
        }

        for (int i = 0; i < len; i++) {
            for (int j = i; j < len; j++) {
                if (getParent(groups, i) == getParent(groups, j)) continue;
                if (isLinked(frames, colNames, groups, i, j)) {
                    setParent(groups, j, i);
                }
            }
        }
        HashSet<String> dict = new HashSet<>();
        for (int i = 0; i < groups.length; i++) {
            dict.add(String.valueOf(groups[i]));
        }
        int pos = 0;
        for (Frame f : frames) {
            Vector nom = new NominalVector(relationName, f.getRowCount(), dict);
            for (int i = 0; i < f.getRowCount(); i++) {
                nom.setLabel(i, String.valueOf(groups[pos++]));
            }
            List<Vector> vectors = new ArrayList<>();
            for (int i = 0; i < f.getColCount(); i++) {
                vectors.add(f.getCol(i));
            }
            vectors.add(nom);
            Frame ff = new SolidFrame(f.getName(), f.getRowCount(), vectors);
            result.add(ff);
        }

        return result;
    }

    private static boolean isLinked(List<Frame> frames, String[] colNames, int[] groups, int i, int j) {
        int indexi = 0;
        for (int l = 0; l < frames.size(); l++) {
            if (i < frames.get(l).getRowCount()) {
                indexi = l;
                break;
            }
            i -= frames.get(l).getRowCount();
        }
        int indexj = 0;
        for (int l = 0; l < frames.size(); l++) {
            if (j < frames.get(l).getRowCount()) {
                indexj = l;
                break;
            }
            j -= frames.get(l).getRowCount();
        }
        for (int k = 0; k < colNames.length; k++) {
            String colName = colNames[k];
            Vector vi = frames.get(indexi).getCol(colName);
            Vector vj = frames.get(indexj).getCol(colName);

            if (vi.isNominal() && !vi.isMissing(i)) {
                String[] left = vi.getLabel(i).split(" ");
                String[] right = vj.getLabel(j).split(" ");
                for (int l = 0; l < left.length; l++) {
                    for (int m = 0; m < right.length; m++) {
                        if (left[l].equals(right[m])) return true;
                    }
                }
            }
        }
        return false;
    }

    private static void setParent(int[] groups, int to, int parent) {
        while (true) {
            if (to == parent) return;
            if (groups[to] == to) {
                groups[to] = parent;
                return;
            }
            int next = groups[to];
            groups[to] = parent;
            to = next;
        }
    }

    private static int getParent(int[] groups, int i) {
        while (groups[i] != i) {
            i = groups[i];
        }
        return i;
    }
}
