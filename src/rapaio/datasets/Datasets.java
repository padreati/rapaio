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

package rapaio.datasets;

import rapaio.data.Frame;
import rapaio.data.VectorType;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Datasets {

    public static Frame loadIrisDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withDefaultType(VectorType.NUMERIC)
                .withNominalFields("class")
                .read(Datasets.class, "iris.csv");
    }

    public static Frame loadPearsonHeightDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withDefaultType(VectorType.NUMERIC)
                .read(Datasets.class, "pearsonheight.csv");
    }

    public static Frame loadChestDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withSeparatorChar(',')
                .withQuotas(true)
                .withDefaultType(VectorType.NUMERIC)
                .read(Datasets.class, "chest.csv");
    }

    public static Frame loadCarMpgDataset() throws IOException, URISyntaxException {
        return new Csv()
                .withSeparatorChar(',')
                .withHeader(true)
                .withQuotas(true)
                .withDefaultType(VectorType.NUMERIC)
                .read(Datasets.class, "carmpg.csv");
    }

    public static Frame loadSpamBase() throws IOException, URISyntaxException {
        return new Csv().withDefaultType(VectorType.NUMERIC)
                .withNominalFields("spam")
                .read(Datasets.class, "spam-base.csv");
    }

    public static Frame loadMushrooms() throws IOException {
        return new Csv()
                .withSeparatorChar(',')
                .withHeader(true)
                .withQuotas(false)
                .read(Datasets.class, "mushrooms.csv");
    }

    public static Frame loadPlay() throws IOException {
        return new Csv()
                .withSeparatorChar(',')
                .withHeader(true)
                .withQuotas(false)
                .withNumericFields("temp", "humidity")
                .read(Datasets.class, "play.csv");
    }

    public static Frame loadOlympic() throws IOException {
        return new Csv()
                .withQuotas(false)
                .withNumericFields("Edition")
                .read(Datasets.class, "olympic.csv");
    }

    public static Frame loadProstateCancer() throws IOException {
        return new Csv()
                .withSeparatorChar('\t')
                .withDefaultType(VectorType.NUMERIC)
                .read(Datasets.class, "prostate.csv");
    }
}
