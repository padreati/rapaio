package sample;

import rapaio.data.Vector;
import rapaio.distributions.DUniform;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sandbox {

    public static void main(String[] args) {
        simulation();
    }

    public static void simulation() {

        DUniform distr = new DUniform(1, 6);
        double first = 0;
        double second = 0;
        Vector v = distr.sample(10000000);
        for (int i = 0; i < v.getRowCount(); i++) {
            if (i % 1000 == 0) {
                System.out.print(String.format("skip=%.2f, notskip=%.2f, ", first / i, second / i));
                System.out.println();
            }
            if (v.getIndex(i) == 1) {
                continue;
            }
            if (v.getIndex(i) == 2) {
                second++;
                continue;
            }
            if (v.getIndex(i) == 3) {
                first++;
                continue;
            }
            first++;
            second++;
        }
    }
}
