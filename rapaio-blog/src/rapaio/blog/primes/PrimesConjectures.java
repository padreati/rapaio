
package rapaio.blog.primes;

import rapaio.data.*;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.io.CsvPersistence;
import rapaio.server.AbstractCmd;

import java.io.IOException;

import static rapaio.core.BaseMath.sqrt;
import static rapaio.workspace.Workspace.draw;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PrimesConjectures extends AbstractCmd {

    private final String file = "/home/ati/work/rapaio-data/primes/primes.txt";

    public static void main(String[] args) {
        new PrimesConjectures().runLocal();
    }

    @Override
    public void run() {
        Vector primes = loadPrimesVector();
//        runChebychevConjecture(primes);
        runAndricaConjecture(primes);
    }

    private Vector loadPrimesVector() {
        CsvPersistence csv = new CsvPersistence();
        csv.setHasHeader(true);
        csv.getIndexFieldHints().add("primes");
        try {
            Frame df = csv.read(file);
            return df.getCol(0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void runChebychevConjecture(Vector primes) {
        final int SIZE = primes.getRowCount();
        final int STEP = 1000;
        int[] count = new int[4];
        Frame matrix = Frames.newMatrixFrame((SIZE / STEP) + 1, new String[]{"index", "c1", "c3", "delta"});

        for (int i = 1; i < SIZE; i++) {
            count[primes.getIndex(i) % 4]++;
            if (i % STEP == 0) {
                matrix.setValue(i / STEP, "index", i + 1);
                matrix.setValue(i / STEP, "c1", count[1]);
                matrix.setValue(i / STEP, "c3", count[1]);
                matrix.setValue(i / STEP, "delta", count[3] - count[1]);
                matrix.setValue(i, file, i);
            }
        }

        draw(new Plot()
                .add(new Lines(matrix.getCol("index"), matrix.getCol("delta")))
                .setLwd(0.1f)
        );
        draw(new Plot()
                .add(new Lines(matrix.getCol("index"), matrix.getCol("delta")))
                .setLwd(0.1f)
        );
    }

    private void runAndricaConjecture(Vector primes) {
        final int SIZE = primes.getRowCount() - 1;
        Vector delta = new NumVector(SIZE);
        Vector index = Vectors.newIdx(SIZE);
        for (int i = 0; i < SIZE; i++) {
            delta.setValue(i, sqrt(primes.getValue(i + 1)) - sqrt(primes.getValue(i)));
            index.setIndex(i, i + 1);
        }
//        draw(new Plot().add(new Lines(index, delta)).setYRange(0, 0.02));
        draw(new Plot().add(new Points(index, delta)).setYRange(0, 0.02));
    }
}
