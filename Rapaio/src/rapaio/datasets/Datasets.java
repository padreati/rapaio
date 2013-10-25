/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.datasets;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.filters.BaseFilters;
import rapaio.io.ArffPersistence;
import rapaio.io.CsvPersistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Datasets {

    public static Frame loadIrisDataset() throws IOException {
        Frame df;
        try (InputStream is = Datasets.class.getResourceAsStream("iris.csv")) {
            df = new CsvPersistence().read("iris", is);
        }
        Vector[] vectors = new Vector[df.getColCount()];
        vectors[vectors.length - 1] = df.getCol(vectors.length - 1);
        for (int i = 0; i < vectors.length - 1; i++) {
            vectors[i] = BaseFilters.toNumeric(df.getCol(i).getName(), df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public static Frame loadPearsonHeightDataset() throws IOException {
        Frame df = new CsvPersistence().read("pearson", Datasets.class.getResourceAsStream("pearsonheight.csv"));
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < df.getColCount(); i++) {
            vectors[i] = BaseFilters.toNumeric(df.getCol(i).getName(), df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public static Frame loadChestDataset() throws IOException {
        CsvPersistence persistence = new CsvPersistence();
        persistence.setColSeparator(',');
        persistence.setHasQuotas(false);
        Frame df = persistence.read("chest", Datasets.class.getResourceAsStream("chest.csv"));
        return BaseFilters.toNumeric(df);
    }

    public static Frame loadCarMpgDataset() throws IOException {
        CsvPersistence persistence = new CsvPersistence();
        persistence.setColSeparator(',');
        persistence.setHasHeader(true);
        persistence.setHasQuotas(false);
        Frame df = persistence.read("carmpg", Datasets.class.getResourceAsStream("carmpgdat.csv"));
        Vector[] vectors = new Vector[df.getColCount()];
        vectors[0] = df.getCol(0);
        vectors[1] = df.getCol(1);
        for (int i = 2; i < df.getColCount(); i++) {
            vectors[i] = BaseFilters.toNumeric(df.getCol(i).getName(), df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public static Frame loadSpamBase() throws IOException {
        CsvPersistence persistence = new CsvPersistence();
        persistence.setColSeparator(',');
        persistence.setHasHeader(true);
        Frame df = persistence.read("spam-base", Datasets.class.getResourceAsStream("spam-base.csv"));
        List<Vector> vectors = new ArrayList<>();
        for (int i = 0; i < df.getColCount() - 1; i++) {
            vectors.add(BaseFilters.toNumeric(df.getCol(i).getName(), df.getCol(i)));
        }
        vectors.add(df.getCol(df.getColCount() - 1));
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }
}
