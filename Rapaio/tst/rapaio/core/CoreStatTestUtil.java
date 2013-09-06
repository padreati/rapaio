package rapaio.core;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.io.CsvPersistence;

import java.io.IOException;

import static rapaio.core.BaseFilters.toValue;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class CoreStatTestUtil {

    private Frame df;

    public CoreStatTestUtil() throws IOException {
        CsvPersistence p = new CsvPersistence();
        p.setHeader(false);
        df = p.read("core_stat", CoreStatTestUtil.class.getResourceAsStream("core_stat.csv"));
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = toValue(df.getCol(i));
        }
        df = new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public Frame getDataFrame() {
        return df;
    }
}