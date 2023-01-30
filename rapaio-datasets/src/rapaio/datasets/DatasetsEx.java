package rapaio.datasets;

import java.io.IOException;

import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.io.Csv;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DatasetsEx {

    private DatasetsEx() {
    }

    public static Frame loadChestDataset() throws IOException {
        return Csv.instance()
                .separatorChar.set(',')
                .quotes.set(true)
                .defaultTypes.set(VarType.DOUBLE)
                .read(Datasets.class, "chest.csv");
    }

    public static Frame loadMtcars() throws IOException {
        return Csv.instance()
                .read(Datasets.class, "mtcars.csv");
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

    public static Frame loadCoverType() throws IOException {
        return Csv.instance()
                .quotes.set(true)
                .read(Datasets.class.getResourceAsStream("covtype.csv"));
    }


    public static Frame loadVowelTrain() {
        return Csv.instance()
                .keepCols.set(t -> t != 0)
                .types.add(VarType.NOMINAL, "y")
                .readUrl("https://web.stanford.edu/~hastie/ElemStatLearn/datasets/vowel.train");
    }

    public static Frame loadVowelTest() {
        return Csv.instance()
                .keepCols.set(t -> t != 0)
                .types.add(VarType.NOMINAL, "y")
                .readUrl("https://web.stanford.edu/~hastie/ElemStatLearn/datasets/vowel.test");
    }
}
