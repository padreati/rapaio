package rapaio.data.filter;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FFilterTestUtil {

    public static Frame allDoubles(int n, int k) {
        Var[] vars = new Var[k];
        for (int i = 0; i < k; i++) {
            vars[i] = VarDouble.from(n, row -> RandomSource.nextDouble() - 0.5).name("V" + (i + 1));
        }
        return SolidFrame.byVars(vars);
    }

    public static Frame allDoubleNominal(int n, int dCount, int nomCunt) {
        int len = dCount + nomCunt;
        Var[] vars = new Var[len];

        String[] words = new String[]{
                "a", "factor", "base", "spectrum", "glance", "point", "shuffle", "bias"
        };

        for (int i = 0; i < len; i++) {
            if (i < dCount) {
                vars[i] = VarDouble.from(n, row -> RandomSource.nextDouble() - 0.5)
                        .name("v" + (i + 1));
            } else {
                vars[i] = VarNominal.from(n, row -> words[RandomSource.nextInt(words.length)])
                        .name("v" + (i + 1));
            }
        }
        return SolidFrame.byVars(vars);
    }
}
