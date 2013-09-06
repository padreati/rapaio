package rapaio.datasets;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.io.CsvPersistence;

import java.io.IOException;

import static rapaio.core.BaseFilters.toValue;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Datasets {

    public static Frame loadIrisDataset() throws IOException {
        Frame df = new CsvPersistence().read("iris", Datasets.class.getResourceAsStream("iris.csv"));
        Vector[] vectors = new Vector[df.getColCount()];
        vectors[vectors.length - 1] = df.getCol(vectors.length - 1);
        for (int i = 0; i < vectors.length - 1; i++) {
            vectors[i] = toValue(df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public static Frame loadPearsonHeightDataset() throws IOException {
        Frame df = new CsvPersistence().read("pearson", Datasets.class.getResourceAsStream("pearsonheight.csv"));
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < df.getColCount(); i++) {
            vectors[i] = toValue(df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }
}
