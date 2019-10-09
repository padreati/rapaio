/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
 *
 */

package rapaio.data;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarDoubleTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(134);
    }

    @Test
    public void testEmptyWithNoRows() {
        VarDouble empty = VarDouble.empty();
        assertEquals(0, empty.rowCount());
    }

    @Test
    public void testVarEmptyWithRows() {
        VarDouble empty = VarDouble.empty(100);
        assertEquals(100, empty.rowCount());
        for (int i = 0; i < 100; i++) {
            assertTrue(empty.isMissing(i));
        }
    }

    @Test
    public void testStaticBuilders() {
        int[] sourceIntArray = IntStream.range(0, 100).map(i -> (i % 10 == 0) ? Integer.MIN_VALUE : RandomSource.nextInt(100)).toArray();
        List<Integer> sourceIntList = Arrays.stream(sourceIntArray).boxed().collect(Collectors.toList());

        VarDouble copy = VarDouble.copy(sourceIntArray);
        assertEquals(100, copy.rowCount());
        for (int i = 0; i < 100; i++) {
            assertEquals(sourceIntArray[i], copy.getDouble(i), TOL);
        }
        assertTrue(copy.deepEquals(VarDouble.copy(sourceIntList)));
        assertTrue(copy.deepEquals(VarDouble.copy(copy)));
        assertTrue(copy.deepEquals(VarDouble.copy(VarInt.wrap(sourceIntArray))));

        double[] sourceDoubleArray = IntStream.range(0, 100).mapToDouble(i -> (i % 10 == 0) ? Double.NaN : RandomSource.nextDouble()).toArray();
        List<Double> sourceDoubleList = Arrays.stream(sourceDoubleArray).boxed().collect(Collectors.toList());

        VarDouble dcopy = VarDouble.copy(sourceDoubleArray);
        assertEquals(100, dcopy.rowCount());
        for (int i = 0; i < dcopy.rowCount(); i++) {
            assertEquals(sourceDoubleArray[i], dcopy.getDouble(i), TOL);
        }
        assertTrue(dcopy.deepEquals(VarDouble.copy(dcopy)));
        assertTrue(dcopy.deepEquals(VarDouble.wrap(sourceDoubleArray)));
        assertTrue(dcopy.deepEquals(VarDouble.wrap(new DoubleArrayList(sourceDoubleArray))));
        assertTrue(dcopy.deepEquals(VarDouble.from(100, dcopy::getDouble)));

        Iterator<Double> it = sourceDoubleList.iterator();
        assertTrue(dcopy.deepEquals(VarDouble.from(100, it::next)));
        assertTrue(dcopy.deepEquals(VarDouble.from(dcopy, val -> val)));

        VarDouble fill1 = VarDouble.fill(100);
        assertEquals(100, fill1.rowCount());
        fill1.stream().mapToDouble().forEach(val -> assertEquals(0.0, val, TOL));

        VarDouble fill2 = VarDouble.fill(100, 20);
        assertEquals(100, fill2.rowCount());
        fill2.stream().mapToDouble().forEach(val -> assertEquals(20.0, val, TOL));
        assertTrue(VarDouble.empty().deepEquals(fill2.newInstance(0)));

        VarDouble seq1 = VarDouble.seq(100);
        VarDouble seq2 = VarDouble.seq(0, 100);
        VarDouble seq3 = VarDouble.seq(0, 100, 1);

        assertTrue(seq1.deepEquals(seq2));
        assertTrue(seq1.deepEquals(seq3));
    }

    @Test
    public void smokeTest() {
        Var v = VarDouble.empty();
        boolean flag = v.type().isNumeric();
        assertTrue(flag);
        assertFalse(v.type().isNominal());

        assertEquals(0, v.rowCount());
        assertEquals("VarDouble [name:\"?\", rowCount:1, values: ?]", VarDouble.empty(1).toString());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Illegal row count: -1");
        VarDouble.empty(-1);
    }

    @Test
    public void testGetterSetter() {
        Var v = VarDouble.from(10, i -> Math.log(10 + i));

        for (int i = 0; i < 10; i++) {
            assertEquals(Math.log(10 + i), v.getDouble(i), 1e-10);
            assertEquals((int) Math.rint(Math.log(10 + i)), v.getInt(i));
        }

        for (int i = 0; i < 10; i++) {
            v.setInt(i, i * i);
            assertEquals(i * i, v.getInt(i));
            assertEquals(i * i, v.getDouble(i), 1e-10);
            assertEquals(String.valueOf(v.getDouble(i)), v.getLabel(i));
        }
    }

    @Test
    public void testSetUnparsableString() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"test\"");
        VarDouble.scalar(10).setLabel(0, "test");
    }

    @Test
    public void testAddUnparsableLabel() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"x\"");
        VarDouble.scalar(10).addLabel("x");
    }

    @Test
    public void testGetLevels() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for double vectors.");
        VarDouble.scalar(10).levels();
    }

    @Test
    public void testSetLeveles() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for double vectors.");
        VarDouble.scalar(10).setLevels(new String[]{});
    }

    @Test
    public void testOneNumeric() {
        Var one = VarDouble.scalar(Math.PI);

        assertEquals(1, one.rowCount());
        assertEquals(Math.PI, one.getDouble(0), 1e-10);

        one = VarDouble.scalar(Math.E);
        assertEquals(1, one.rowCount());
        assertEquals(Math.E, one.getDouble(0), 1e-10);
    }

    @Test
    public void testWithName() {
        VarDouble x = VarDouble.copy(1, 2, 3, 5).withName("X");
        assertEquals("X", x.name());

        Var y = MappedVar.byRows(x, 1, 2);
        assertEquals("X", y.name());
        y.withName("y");
        assertEquals("y", y.name());

        assertEquals(2.0, y.getDouble(0), 10e-10);
        assertEquals(3.0, y.getDouble(1), 10e-10);
    }

    @Test
    public void testOtherValues() {
        VarDouble x = VarDouble.copy(1, 2, 3, 4).withName("x");

        x.addInt(10);
        assertEquals(10, x.getDouble(x.rowCount() - 1), 10e-10);

        VarDouble s = VarDouble.empty();
        s.addLong(1);
        s.addLong(-100000000000L);
        assertEquals(1L, s.getLong(0));
        assertEquals(-100000000000d, s.getLong(1), 10e-10);

        s.setLong(1, 15);
        assertEquals(15, s.getLong(1));


        VarDouble mis = VarDouble.empty();
        mis.addMissing();
        mis.addDouble(1);
        mis.addMissing();
        mis.addDouble(2);
        mis.setMissing(3);

        assertTrue(mis.isMissing(0));
        assertTrue(mis.isMissing(2));
        assertTrue(mis.isMissing(3));
        assertFalse(mis.isMissing(1));
    }

    @Test
    public void testClearRemove() {
        VarDouble x = VarDouble.copy(1, 2, 3);
        VarDouble x2 = VarDouble.copy(x);
        x.removeRow(1);

        assertEquals(1, x.getInt(0));
        assertEquals(3, x.getInt(1));

        VarDouble y = x.copy();

        x.clearRows();

        assertEquals(0, x.rowCount());

        assertEquals(2, y.rowCount());
        assertEquals(1, y.getInt(0));
        assertEquals(3, y.getInt(1));

        x2.addRows(3);

        assertEquals(6, x2.rowCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, x2.getDouble(i), TOL);
            assertTrue(x2.isMissing(i + 3));
        }
    }

    @Test
    public void testLabelOperations() {
        VarDouble var = VarDouble.wrap(1.0, 1.0, 1.0, 1.0);

        var.setLabel(0, "?");
        var.setLabel(1, "Inf");
        var.setLabel(2, "-Inf");
        var.setLabel(3, "-10.3");

        var.addLabel("?");
        var.addLabel("Inf");
        var.addLabel("-Inf");
        var.addLabel("-10.3");

        double[] expected = new double[]{
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -10.3,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -10.3};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], var.getDouble(i), TOL);
        }
    }

    @Test
    public void testCollector() {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(RandomSource.nextDouble() * 100);
        }
        VarDouble copy = list.stream().collect(VarDouble.collector());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), copy.getDouble(i), TOL);
        }
    }

    @Test
    public void testString() {
        Var x = VarDouble.from(10, row -> (row % 4 == 0) ? Double.NaN : Normal.std().sampleNext());

        assertEquals("VarDouble [name:\"?\", rowCount:10, values: ?, 0.6503131914222008, 1.1647628389666604, 0.7719984559060187, ?, 2.1236947978859986, 1.6546944254696838, 0.12053767260217511, ?, -0.01950154486410645]",
                x.toString());

        WS.getPrinter().withTextWidth(100);

        x = VarDouble.from(200, row -> (row % 4 == 0) ? Double.NaN : Normal.std().sampleNext());
        assertEquals("VarDouble [name:\"?\", rowCount:200]\n" +
                " row    value     row    value     row    value     row    value     row    value     row    value    \n" +
                "  [0]     ?       [17]  2.0871387  [34] -0.0936266  [51]  1.852116   [68]     ?      [185] -0.6420142 \n" +
                "  [1]  0.3902811  [18] -0.6484484  [35]  1.2676072  [52]     ?       [69] -0.8811788 [186]  0.1204142 \n" +
                "  [2] -0.496926   [19] -0.2022233  [36]     ?       [53] -1.1320114  [70]  0.5364375 [187] -0.573785  \n" +
                "  [3] -0.0845844  [20]     ?       [37] -0.3564991  [54]  0.5508957  [71] -1.0112584 [188]     ?      \n" +
                "  [4]     ?       [21] -1.0826334  [38] -0.0510566  [55]  1.4914125  [72]     ?      [189] -1.930123  \n" +
                "  [5]  1.1512637  [22] -0.8585979  [39] -0.3128423  [56]     ?       [73] -0.9710497 [190]  0.8269811 \n" +
                "  [6]  0.208716   [23] -1.6010329  [40]     ?       [57] -0.0944478  [74]  0.3620872 [191] -0.2022489 \n" +
                "  [7]  0.1693098  [24]     ?       [41] -0.4520359  [58]  0.3082581  [75]  1.2479583 [192]     ?      \n" +
                "  [8]     ?       [25]  0.3697439  [42] -0.4842271  [59] -1.470797   [76]     ?      [193] -0.5294699 \n" +
                "  [9] -1.1062647  [26] -0.866674   [43]  0.584072   [60]     ?       [77]  0.4264658 [194]  1.7129322 \n" +
                " [10]  0.9310864  [27]  0.2575936  [44]     ?       [61] -2.0059045  [78]  1.4864833 [195] -1.2866207 \n" +
                " [11] -0.2473742  [28]     ?       [45]  1.6808275  [62]  1.7896726  ...     ...     [196]     ?      \n" +
                " [12]     ?       [29] -0.5367294  [46]  0.1817162  [63]  0.1074123 [180]     ?      [197]  1.1222908 \n" +
                " [13] -1.1560236  [30]  0.6122596  [47]  0.4379756  [64]     ?      [181]  1.433059  [198] -0.0382304 \n" +
                " [14]  0.3677809  [31]  0.6283116  [48]     ?       [65]  0.1505781 [182]  0.2138017 [199] -1.0190256 \n" +
                " [15]  0.0442839  [32]     ?       [49]  1.2946902  [66] -0.2627364 [183]  0.1037009                  \n" +
                " [16]     ?       [33] -0.0846767  [50]  2.3935302  [67]  0.1594684 [184]     ?      \n", x.content());

        assertEquals("VarDouble [name:\"?\", rowCount:200]\n" +
                " row    value     row    value     row    value     row    value     row    value     row    value    \n" +
                "  [0]     ?       [34] -0.0936266  [68]     ?      [102] -0.3391072 [136]     ?      [170] -0.346324  \n" +
                "  [1]  0.3902811  [35]  1.2676072  [69] -0.8811788 [103]  1.0439885 [137] -1.3973117 [171] -0.04914   \n" +
                "  [2] -0.496926   [36]     ?       [70]  0.5364375 [104]     ?      [138] -0.2413323 [172]     ?      \n" +
                "  [3] -0.0845844  [37] -0.3564991  [71] -1.0112584 [105]  1.1305554 [139] -1.4363241 [173]  1.0411656 \n" +
                "  [4]     ?       [38] -0.0510566  [72]     ?      [106]  0.8184613 [140]     ?      [174]  0.9358435 \n" +
                "  [5]  1.1512637  [39] -0.3128423  [73] -0.9710497 [107] -1.3279566 [141]  0.0984271 [175] -0.3420647 \n" +
                "  [6]  0.208716   [40]     ?       [74]  0.3620872 [108]     ?      [142]  1.3530075 [176]     ?      \n" +
                "  [7]  0.1693098  [41] -0.4520359  [75]  1.2479583 [109]  0.8183007 [143]  1.1810744 [177] -0.1876596 \n" +
                "  [8]     ?       [42] -0.4842271  [76]     ?      [110]  2.0939328 [144]     ?      [178]  0.9617247 \n" +
                "  [9] -1.1062647  [43]  0.584072   [77]  0.4264658 [111]  1.1161191 [145] -1.170265  [179] -0.1411269 \n" +
                " [10]  0.9310864  [44]     ?       [78]  1.4864833 [112]     ?      [146]  0.849503  [180]     ?      \n" +
                " [11] -0.2473742  [45]  1.6808275  [79] -1.0755159 [113]  0.3136267 [147]  0.4583235 [181]  1.433059  \n" +
                " [12]     ?       [46]  0.1817162  [80]     ?      [114] -0.537602  [148]     ?      [182]  0.2138017 \n" +
                " [13] -1.1560236  [47]  0.4379756  [81]  0.6557535 [115]  0.6442586 [149] -2.5426738 [183]  0.1037009 \n" +
                " [14]  0.3677809  [48]     ?       [82]  1.3529534 [116]     ?      [150]  0.243744  [184]     ?      \n" +
                " [15]  0.0442839  [49]  1.2946902  [83]  0.4104372 [117]  0.9488238 [151] -0.1938986 [185] -0.6420142 \n" +
                " [16]     ?       [50]  2.3935302  [84]     ?      [118]  1.2261407 [152]     ?      [186]  0.1204142 \n" +
                " [17]  2.0871387  [51]  1.852116   [85]  0.737652  [119]  0.2295242 [153] -1.0990664 [187] -0.573785  \n" +
                " [18] -0.6484484  [52]     ?       [86] -0.7766432 [120]     ?      [154]  0.8191411 [188]     ?      \n" +
                " [19] -0.2022233  [53] -1.1320114  [87]  2.006738  [121]  0.183381  [155]  0.7662643 [189] -1.930123  \n" +
                " [20]     ?       [54]  0.5508957  [88]     ?      [122] -1.0371901 [156]     ?      [190]  0.8269811 \n" +
                " [21] -1.0826334  [55]  1.4914125  [89]  0.4654413 [123]  0.832519  [157] -0.2870929 [191] -0.2022489 \n" +
                " [22] -0.8585979  [56]     ?       [90] -1.4516869 [124]     ?      [158] -0.8243025 [192]     ?      \n" +
                " [23] -1.6010329  [57] -0.0944478  [91]  0.1678896 [125]  1.062308  [159] -0.5484135 [193] -0.5294699 \n" +
                " [24]     ?       [58]  0.3082581  [92]     ?      [126] -0.3063072 [160]     ?      [194]  1.7129322 \n" +
                " [25]  0.3697439  [59] -1.470797   [93] -0.3532948 [127] -0.5180611 [161]  0.0689444 [195] -1.2866207 \n" +
                " [26] -0.866674   [60]     ?       [94] -1.3440782 [128]     ?      [162]  0.3117683 [196]     ?      \n" +
                " [27]  0.2575936  [61] -2.0059045  [95] -1.1434814 [129]  1.5282007 [163] -0.2889222 [197]  1.1222908 \n" +
                " [28]     ?       [62]  1.7896726  [96]     ?      [130]  1.2726592 [164]     ?      [198] -0.0382304 \n" +
                " [29] -0.5367294  [63]  0.1074123  [97] -1.1405    [131]  0.264365  [165]  1.6684605 [199] -1.0190256 \n" +
                " [30]  0.6122596  [64]     ?       [98] -0.9974311 [132]     ?      [166]  0.0564739 \n" +
                " [31]  0.6283116  [65]  0.1505781  [99] -0.8355704 [133]  1.0627032 [167] -1.1645925 \n" +
                " [32]     ?       [66] -0.2627364 [100]     ?      [134]  0.5067461 [168]     ?      \n" +
                " [33] -0.0846767  [67]  0.1594684 [101] -0.228796  [135] -1.4055612 [169] -1.7678708 \n", x.fullContent());
    }

    @Test
    public void testSequence() {
        assertTrue(VarDouble.seq(0, 0.9, 0.3).deepEquals(VarDouble.from(4, r -> r * 0.3)));
        assertTrue(VarDouble.seq(-1, 1, 0.25).deepEquals(VarDouble.from(9, r -> r * 0.25 - 1)));
    }
}
