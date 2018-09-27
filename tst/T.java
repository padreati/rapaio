import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/27/18.
 */
public class T {

    public static void main(String[] args) {

        double[] x = VarDouble.from(1000, RandomSource::nextDouble).stream().mapToDouble().toArray();
        double[] y = Arrays.stream(x).flatMap(v -> DoubleStream.iterate(0, row -> row+1)).limit(100).toArray();
    }
}
