package rapaio.math.linear.dense;

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/3/19.
 */
public class DenseDVectorTest {

    private static final double TOL = 1e-100;

    @Test
    void testBuilders() {
        assertEqualVector(DenseDVector.wrap(1, 2, 3, 4, 5, 6, 7), new double[]{1, 2, 3, 4, 5, 6, 7});
        assertEqualVector(DenseDVector.zeros(3), new double[]{0, 0, 0});
        assertEqualVector(DenseDVector.ones(4), new double[]{1, 1, 1, 1});
        assertEqualVector(DenseDVector.wrap(VarDouble.seq(1, 10, 1)), new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    }


    private void assertEqualVector(DVector vector, double[] array) {
        assertEquals(1, vector.ndim());
        assertEquals(array.length, vector.shape()[0]);
        assertEquals(array.length, vector.size());

        for (int i = 0; i < vector.size(); i++) {
            assertEquals(array[i], vector.get(i), TOL);
        }
    }

    @Test
    void testToString() {
        assertEquals("DenseDVector{size=5, values=1,1,1,1,1}", DenseDVector.ones(5).toString());
        assertEquals("DenseDVector{size=12, values=1,1,1,1,1,1,1,1,1,1,1,1,...}", DenseDVector.ones(12).toString());
        assertEquals("DenseDVector{size=11, values=1,1,1,1,1,1,1,1,1,1,1}", DenseDVector.ones(11).toString());
    }

    @Test
    void testToContent() {
        assertEquals(" row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value \n" +
                        "  [0]   0    [11]   2.2  [22]   4.4  [33]   6.6  [44]   8.8  [55]  11    [66]  13.2  [77]  15.4 [489]  97.8 [500] 100   \n" +
                        "  [1]   0.2  [12]   2.4  [23]   4.6  [34]   6.8  [45]   9    [56]  11.2  [67]  13.4  [78]  15.6 [490]  98               \n" +
                        "  [2]   0.4  [13]   2.6  [24]   4.8  [35]   7    [46]   9.2  [57]  11.4  [68]  13.6  ...   ...  [491]  98.2 \n" +
                        "  [3]   0.6  [14]   2.8  [25]   5    [36]   7.2  [47]   9.4  [58]  11.6  [69]  13.8 [481]  96.2 [492]  98.4 \n" +
                        "  [4]   0.8  [15]   3    [26]   5.2  [37]   7.4  [48]   9.6  [59]  11.8  [70]  14   [482]  96.4 [493]  98.6 \n" +
                        "  [5]   1    [16]   3.2  [27]   5.4  [38]   7.6  [49]   9.8  [60]  12    [71]  14.2 [483]  96.6 [494]  98.8 \n" +
                        "  [6]   1.2  [17]   3.4  [28]   5.6  [39]   7.8  [50]  10    [61]  12.2  [72]  14.4 [484]  96.8 [495]  99   \n" +
                        "  [7]   1.4  [18]   3.6  [29]   5.8  [40]   8    [51]  10.2  [62]  12.4  [73]  14.6 [485]  97   [496]  99.2 \n" +
                        "  [8]   1.6  [19]   3.8  [30]   6    [41]   8.2  [52]  10.4  [63]  12.6  [74]  14.8 [486]  97.2 [497]  99.4 \n" +
                        "  [9]   1.8  [20]   4    [31]   6.2  [42]   8.4  [53]  10.6  [64]  12.8  [75]  15   [487]  97.4 [498]  99.6 \n" +
                        " [10]   2    [21]   4.2  [32]   6.4  [43]   8.6  [54]  10.8  [65]  13    [76]  15.2 [488]  97.6 [499]  99.8 \n",
                DenseDVector.wrap(VarDouble.seq(0, 100, 0.2)).toContent());

        assertEquals("row  value row  value row  value row  value row  value row  value row  value \n" +
                " [0]  0     [8]  1.6  [16]  3.2  [24]  4.8  [32]  6.4  [40]  8    [48]  9.6  \n" +
                " [1]  0.2   [9]  1.8  [17]  3.4  [25]  5    [33]  6.6  [41]  8.2  [49]  9.8  \n" +
                " [2]  0.4  [10]  2    [18]  3.6  [26]  5.2  [34]  6.8  [42]  8.4  [50] 10    \n" +
                " [3]  0.6  [11]  2.2  [19]  3.8  [27]  5.4  [35]  7    [43]  8.6  \n" +
                " [4]  0.8  [12]  2.4  [20]  4    [28]  5.6  [36]  7.2  [44]  8.8  \n" +
                " [5]  1    [13]  2.6  [21]  4.2  [29]  5.8  [37]  7.4  [45]  9    \n" +
                " [6]  1.2  [14]  2.8  [22]  4.4  [30]  6    [38]  7.6  [46]  9.2  \n" +
                " [7]  1.4  [15]  3    [23]  4.6  [31]  6.2  [39]  7.8  [47]  9.4  \n", DenseDVector.wrap(VarDouble.seq(0, 10, 0.2)).toContent());
    }
}
