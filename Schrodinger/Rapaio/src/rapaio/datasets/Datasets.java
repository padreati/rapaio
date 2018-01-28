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

package rapaio.datasets;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.io.ArffPersistence;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Datasets {

    public static Frame loadIrisDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withDefaultTypes(VarType.NUMERIC)
                .withTypes(VarType.NOMINAL, "class")
                .read(Datasets.class, "iris-r.csv");
    }

    public static Frame loadPearsonHeightDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withDefaultTypes(VarType.NUMERIC)
                .read(Datasets.class, "pearsonheight.csv");
    }

    public static Frame loadChestDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withSeparatorChar(',')
                .withQuotes(true)
                .withDefaultTypes(VarType.NUMERIC)
                .read(Datasets.class, "chest.csv");
    }

    public static Frame loadCarMpgDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withSeparatorChar(',')
                .withHeader(true)
                .withQuotes(true)
                .withDefaultTypes(VarType.NUMERIC)
                .withTypes(VarType.NOMINAL, "carname", "origin")
                .read(Datasets.class, "carmpg.csv");
    }

    public static Frame loadSpamBase() throws IOException {
        return new Csv().withDefaultTypes(VarType.NUMERIC)
                .withTypes(VarType.NOMINAL, "spam")
                .read(Datasets.class, "spam-base.csv");
    }

    public static Frame loadMushrooms() throws IOException {
        return new Csv()
                .withSeparatorChar(',')
                .withHeader(true)
                .withQuotes(false)
                .read(Datasets.class, "mushrooms.csv");
    }

    public static Frame loadPlay() throws IOException {
        return new Csv()
                .withSeparatorChar(',')
                .withHeader(true)
                .withQuotes(false)
                .withTypes(VarType.NUMERIC, "temp", "humidity")
                .withTypes(VarType.NOMINAL, "windy")
                .read(Datasets.class, "play.csv");
    }

    public static Frame loadOlympic() throws IOException {
        return new Csv()
                .withQuotes(false)
                .withTypes(VarType.NUMERIC, "Edition")
                .read(Datasets.class, "olympic.csv");
    }

    public static Frame loadProstateCancer() throws IOException {
        return new Csv()
                .withSeparatorChar('\t')
                .withDefaultTypes(VarType.NUMERIC, VarType.NOMINAL)
                .read(Datasets.class, "prostate.csv");
    }

    public static Frame loadHousing() throws IOException {
        return new Csv()
                .withSeparatorChar(',')
                .withDefaultTypes(VarType.NUMERIC)
//                .withTypes(VarType.BINARY, "CHAS")
                .read(Datasets.class, "housing.csv");
    }

    public static Frame loadLifeScience() throws IOException {
        return new Csv()
                .withSeparatorChar(',')
                .withDefaultTypes(VarType.NUMERIC)
                .withTypes(VarType.NOMINAL, "class")
                .read(Datasets.class.getResourceAsStream("life_science.csv"));
    }

    public static Frame loadISLAdvertising() throws IOException {
        return new Csv()
                .withQuotes(true)
                .withDefaultTypes(VarType.NUMERIC)
                .withTypes(VarType.NOMINAL, "ID")
                .read(Datasets.class.getResourceAsStream("ISL/advertising.csv"));
    }

    public static Frame loadRandom() throws IOException {
        return new Csv()
                .withTypes(VarType.BINARY, "bin")
                .withTypes(VarType.INDEX, "index")
                .withTypes(VarType.NUMERIC, "num")
                .withTypes(VarType.STAMP, "stamp")
                .withTypes(VarType.NOMINAL, "nom")
                .read(Datasets.class.getResourceAsStream("random.csv"));
    }

    public static Frame loadSonar() throws IOException {
        return new ArffPersistence().read(Datasets.class.getResourceAsStream("UCI/sonar.arff"));
    }

    public static Frame loadCoverType() throws IOException {
        return new Csv()
                .read(Datasets.class.getResourceAsStream("covtype.csv"));
    }
}
