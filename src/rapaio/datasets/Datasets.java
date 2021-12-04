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

package rapaio.datasets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.io.ArffPersistence;
import rapaio.io.Csv;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.ensemble.CForest;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Datasets {

    private Datasets() {
    }

    public static Frame loadIrisDataset() {
        try {
            return Csv.instance()
                    .defaultTypes.set(VarType.DOUBLE)
                    .types.add(VarType.NOMINAL, "class")
                    .read(Datasets.class, "iris-r.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Frame loadPearsonHeightDataset() throws IOException {
        return Csv.instance()
                .defaultTypes.set(VarType.DOUBLE)
                .read(Datasets.class, "pearsonheight.csv");
    }

    public static Frame loadISLHitters() throws IOException {
        return Csv.instance()
                .read(Datasets.class, "ISL/Hitters.csv");
    }

    public static Frame loadMtcars() throws IOException {
        return Csv.instance()
                .read(Datasets.class, "mtcars.csv");
    }

    public static Frame loadOldFaithful() {
        try {
            return Csv.instance()
                    .separatorChar.set('\t')
                    .read(Datasets.class, "old_faithful.tsv");
        } catch (IOException e) {
            throw new RuntimeException("Error loading old_faithful.tsv datasets.", e);
        }
    }

    public static Frame loadChestDataset() throws IOException {
        return Csv.instance()
                .separatorChar.set(',')
                .quotes.set(true)
                .defaultTypes.set(VarType.DOUBLE)
                .read(Datasets.class, "chest.csv");
    }

    public static Frame loadCarMpgDataset() throws IOException {
        return Csv.instance()
                .separatorChar.set(',')
                .header.set(true)
                .quotes.set(true)
                .defaultTypes.set(VarType.DOUBLE)
                .types.add(VarType.NOMINAL, "carname", "origin")
                .read(Datasets.class, "carmpg.csv");
    }

    public static Frame loadSpamBase() throws IOException {
        return Csv.instance().defaultTypes.set(VarType.DOUBLE)
                .types.add(VarType.NOMINAL, "spam")
                .read(Datasets.class, "spam-base.csv");
    }

    public static Frame loadMushrooms() {
        try {
            return Csv.instance()
                    .separatorChar.set(',')
                    .header.set(true)
                    .quotes.set(false)
                    .read(Datasets.class, "mushrooms.csv");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Frame loadPlay() {
        try {
            return Csv.instance()
                    .separatorChar.set(',')
                    .header.set(true)
                    .quotes.set(false)
                    .types.add(VarType.DOUBLE, "temp", "humidity")
                    .types.add(VarType.NOMINAL, "windy")
                    .read(Datasets.class, "play.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Frame loadOlympic() throws IOException {
        return Csv.instance()
                .quotes.set(false)
                .types.add(VarType.DOUBLE, "Edition")
                .read(Datasets.class, "olympic.csv");
    }

    public static Frame loadProstateCancer() throws IOException {
        return Csv.instance()
                .separatorChar.set('\t')
                .defaultTypes.set(VarType.DOUBLE, VarType.NOMINAL)
                .read(Datasets.class, "prostate.csv");
    }

    public static Frame loadHousing() throws IOException {
        return Csv.instance()
                .separatorChar.set(',')
                .defaultTypes.set(VarType.DOUBLE)
//                .withTypes(VarType.BINARY, "CHAS")
                .read(Datasets.class, "housing.csv");
    }

    public static Frame loadLifeScience() throws IOException {
        return Csv.instance()
                .separatorChar.set(',')
                .defaultTypes.set(VarType.DOUBLE)
                .types.add(VarType.NOMINAL, "class")
                .read(Datasets.class.getResourceAsStream("life_science.csv"));
    }

    public static Frame loadISLAdvertising() {
        try {
            return Csv.instance()
                    .quotes.set(true)
                    .defaultTypes.set(VarType.DOUBLE)
                    .types.add(VarType.NOMINAL, "ID")
                    .read(Datasets.class.getResourceAsStream("ISL/advertising.csv"))
                    .removeVars("ID")
                    .copy();
        } catch (IOException e) {
            throw new RuntimeException("Dataset could not be load.", e);
        }
    }

    public static Frame loadRandom() {

        int n = 100;
        List<Var> vars = new ArrayList<>();
        vars.add(VarBinary.fromIndex(n,
                row -> row % 7 == 2 ? Integer.MIN_VALUE : RandomSource.nextInt(3) - 1)
                .name("boolean"));
        vars.add(VarDouble.from(n,
                row -> row % 10 == -1 ? Double.NaN : RandomSource.nextDouble())
                .name("double"));
        vars.add(VarInt.from(n,
                row -> row % 13 == 0 ? Integer.MIN_VALUE : RandomSource.nextInt(100) - 50)
                .name("int"));
        vars.add(VarLong.from(n,
                row -> row % 17 == 0 ? Long.MIN_VALUE : 3L * RandomSource.nextInt(Integer.MAX_VALUE))
                .name("long"));
        String[] labels = new String[]{"a", "b", "c", "d", "e"};
        vars.add(VarNominal.from(n,
                row -> row % 17 == 5 ? "?" : labels[RandomSource.nextInt(labels.length)])
                .name("nominal"));
        return SolidFrame.byVars(vars);
    }

    public static Frame loadSonar() throws IOException {
        return new ArffPersistence().read(Datasets.class.getResourceAsStream("UCI/sonar.arff"));
    }

    public static Frame loadCoverType() throws IOException {
        return Csv.instance()
                .quotes.set(true)
                .read(Datasets.class.getResourceAsStream("covtype.csv"));
    }

    public static Frame loasSAheart() {
        try {
            return Csv.instance()
                    .types.add(VarType.NOMINAL, "famhist", "chd")
                    .read(Datasets.class.getResourceAsStream("SAheart.csv"));
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static Frame loadVowelTrain() {
        return Csv.instance()
                .skipCols.set(t -> t == 0)
                .types.add(VarType.NOMINAL, "y")
                .readUrl("https://web.stanford.edu/~hastie/ElemStatLearn/datasets/vowel.train");
    }

    public static Frame loadVowelTest() {
        return Csv.instance()
                .skipCols.set(t -> t == 0)
                .types.add(VarType.NOMINAL, "y")
                .readUrl("https://web.stanford.edu/~hastie/ElemStatLearn/datasets/vowel.test");
    }

    public static void main(String[] args) {
        Frame train = loadVowelTrain();
        train.printSummary();

        ClassifierModel model = CForest.newModel().runs.set(200);
        model.fit(train, "y");

        Frame test = Datasets.loadVowelTest();
        ClassifierResult result = model.predict(test);
        result.printSummary();
    }
}
