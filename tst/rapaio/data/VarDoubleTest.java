/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.sys.With.textWidth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.sys.WS;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarDoubleTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(134);
    }

    @Test
    void testEmptyWithNoRows() {
        VarDouble empty = VarDouble.empty();
        assertEquals(0, empty.size());
    }

    @Test
    void testVarEmptyWithRows() {
        VarDouble empty = VarDouble.empty(100);
        assertEquals(100, empty.size());
        for (int i = 0; i < 100; i++) {
            assertTrue(empty.isMissing(i));
        }
    }

    @Test
    void testStaticBuilders() {
        int[] sourceIntArray = IntStream.range(0, 100).map(i -> (i % 10 == 0) ? Integer.MIN_VALUE : RandomSource.nextInt(100)).toArray();
        List<Integer> sourceIntList = Arrays.stream(sourceIntArray).boxed().collect(Collectors.toList());

        VarDouble copy = VarDouble.copy(sourceIntArray);
        assertEquals(100, copy.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(sourceIntArray[i], copy.getDouble(i), TOL);
        }
        assertTrue(copy.deepEquals(VarDouble.copy(sourceIntList)));
        assertTrue(copy.deepEquals(VarDouble.copy(copy)));
        assertTrue(copy.deepEquals(VarDouble.copy(VarInt.wrap(sourceIntArray))));

        double[] sourceDoubleArray = IntStream.range(0, 100).mapToDouble(i -> (i % 10 == 0) ? Double.NaN : RandomSource.nextDouble()).toArray();
        List<Double> sourceDoubleList = Arrays.stream(sourceDoubleArray).boxed().toList();

        VarDouble dcopy = VarDouble.copy(sourceDoubleArray);
        assertEquals(100, dcopy.size());
        for (int i = 0; i < dcopy.size(); i++) {
            assertEquals(sourceDoubleArray[i], dcopy.getDouble(i), TOL);
        }
        assertTrue(dcopy.deepEquals(VarDouble.copy(dcopy)));
        assertTrue(dcopy.deepEquals(VarDouble.wrap(sourceDoubleArray)));
        assertTrue(dcopy.deepEquals(VarDouble.from(100, dcopy::getDouble)));

        Iterator<Double> it = sourceDoubleList.iterator();
        assertTrue(dcopy.deepEquals(VarDouble.from(100, it::next)));
        assertTrue(dcopy.deepEquals(VarDouble.from(dcopy, val -> val)));

        VarDouble fill1 = VarDouble.fill(100);
        assertEquals(100, fill1.size());
        fill1.stream().mapToDouble().forEach(val -> assertEquals(0.0, val, TOL));

        VarDouble fill2 = VarDouble.fill(100, 20);
        assertEquals(100, fill2.size());
        fill2.stream().mapToDouble().forEach(val -> assertEquals(20.0, val, TOL));
        assertTrue(VarDouble.empty().deepEquals(fill2.newInstance(0)));

        VarDouble seq1 = VarDouble.seq(100);
        VarDouble seq2 = VarDouble.seq(0, 100);
        VarDouble seq3 = VarDouble.seq(0, 100, 1);

        assertTrue(seq1.deepEquals(seq2));
        assertTrue(seq1.deepEquals(seq3));
    }

    @Test
    void smokeTest() {
        Var v = VarDouble.empty();
        boolean flag = v.type().isNumeric();
        assertTrue(flag);
        assertFalse(v.type().isNominal());

        assertEquals(0, v.size());
        assertEquals("VarDouble [name:\"?\", rowCount:1, values: ?]", VarDouble.empty(1).toString());
    }

    @Test
    void testBuildNegativeRowCount() {
        var ex = assertThrows(IllegalArgumentException.class, () -> VarDouble.empty(-1));
        assertEquals("Illegal row count: -1", ex.getMessage());
    }

    @Test
    void testGetterSetter() {
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
    void testSetUnparsableString() {
        var ex = assertThrows(NumberFormatException.class, () -> VarDouble.scalar(10).setLabel(0, "test"));
        assertEquals("For input string: \"test\"", ex.getMessage());
    }

    @Test
    void testAddUnparsableLabel() {
        var ex = assertThrows(NumberFormatException.class, () -> VarDouble.scalar(10).addLabel("x"));
        assertEquals("For input string: \"x\"", ex.getMessage());
    }

    @Test
    void testGetLevels() {
        var ex = assertThrows(RuntimeException.class, () -> VarDouble.scalar(10).levels());
        assertEquals("Operation not available for double vectors.", ex.getMessage());
    }

    @Test
    void testSetLeveles() {
        var ex = assertThrows(RuntimeException.class, () -> VarDouble.scalar(10).setLevels(new String[]{}));
        assertEquals("Operation not available for double vectors.", ex.getMessage());
    }

    @Test
    void testOneNumeric() {
        Var one = VarDouble.scalar(Math.PI);

        assertEquals(1, one.size());
        assertEquals(Math.PI, one.getDouble(0), 1e-10);

        one = VarDouble.scalar(Math.E);
        assertEquals(1, one.size());
        assertEquals(Math.E, one.getDouble(0), 1e-10);
    }

    @Test
    void testWithName() {
        VarDouble x = VarDouble.copy(1, 2, 3, 5).name("X");
        assertEquals("X", x.name());

        Var y = MappedVar.byRows(x, 1, 2);
        assertEquals("X", y.name());
        y.name("y");
        assertEquals("y", y.name());

        assertEquals(2.0, y.getDouble(0), 10e-10);
        assertEquals(3.0, y.getDouble(1), 10e-10);
    }

    @Test
    void testOtherValues() {
        VarDouble x = VarDouble.copy(1, 2, 3, 4).name("x");

        x.addInt(10);
        assertEquals(10, x.getDouble(x.size() - 1), 10e-10);

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
    void testClearRemove() {
        VarDouble x = VarDouble.copy(1, 2, 3);
        VarDouble x2 = VarDouble.copy(x);
        x.removeRow(1);

        assertEquals(1, x.getInt(0));
        assertEquals(3, x.getInt(1));

        VarDouble y = x.copy();

        x.clearRows();

        assertEquals(0, x.size());

        assertEquals(2, y.size());
        assertEquals(1, y.getInt(0));
        assertEquals(3, y.getInt(1));

        x2.addRows(3);

        assertEquals(6, x2.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, x2.getDouble(i), TOL);
            assertTrue(x2.isMissing(i + 3));
        }
    }

    @Test
    void testLabelOperations() {
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
    void testCollector() {
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
    void testString() {
        Var x = VarDouble.from(10, row -> (row % 4 == 0) ? Double.NaN : Normal.std().sampleNext());

        assertEquals("VarDouble [name:\"?\", rowCount:10, values: ?, 0.6503131914222008, 1.1647628389666604, 0.7719984559060187, ?, 2.1236947978859986, 1.6546944254696838, 0.12053767260217511, ?, -0.01950154486410645]",
                x.toString());

        WS.getPrinter().withOptions(textWidth(100));

        x = VarDouble.from(200, row -> (row % 4 == 0) ? Double.NaN : Normal.std().sampleNext());
        assertEquals("""
                VarDouble [name:"?", rowCount:200]
                 row          value          row          value          row          value        \s
                  [0]           ?            [34] -0.09362660880470101   [68]           ?          \s
                  [1]  0.39028109726714816   [35]  1.2676072196723072    [69] -0.8811788136295773  \s
                  [2] -0.4969259911766421    [36]           ?            [70]  0.536437541495931   \s
                  [3] -0.08458437057255112   [37] -0.35649914577043024   [71] -1.0112583879983437  \s
                  [4]           ?            [38] -0.05105659432410365   [72]           ?          \s
                  [5]  1.1512637194768724    [39] -0.31284231287329395   [73] -0.9710496821450608  \s
                  [6]  0.2087159504897916    [40]           ?            [74]  0.3620872239553609  \s
                  [7]  0.1693097723155751    [41] -0.45203587341319634   [75]  1.2479582661214295  \s
                  [8]           ?            [42] -0.4842270880516015    [76]           ?          \s
                  [9] -1.1062646787748283    [43]  0.5840719886385682    [77]  0.4264658345148021  \s
                 [10]  0.9310864075677463    [44]           ?            [78]  1.4864832620223603  \s
                 [11] -0.24737417781497056   [45]  1.6808275328429871    ...           ...         \s
                 [12]           ?            [46]  0.18171623874784112  [180]           ?          \s
                 [13] -1.1560235891200092    [47]  0.43797562020584396  [181]  1.4330590460348056  \s
                 [14]  0.3677808539928047    [48]           ?           [182]  0.21380165834928597 \s
                 [15]  0.04428390568642385   [49]  1.2946902376585687   [183]  0.10370093167281998 \s
                 [16]           ?            [50]  2.3935302203873285   [184]           ?          \s
                 [17]  2.087138707363908     [51]  1.8521159876697453   [185] -0.6420141737488306  \s
                 [18] -0.6484483867259064    [52]           ?           [186]  0.12041419801228942 \s
                 [19] -0.20222328383409388   [53] -1.1320113711661468   [187] -0.5737849693973599  \s
                 [20]           ?            [54]  0.5508956502480397   [188]           ?          \s
                 [21] -1.082633410858406     [55]  1.4914125103602476   [189] -1.930123005115381   \s
                 [22] -0.8585978973902683    [56]           ?           [190]  0.8269811051702728  \s
                 [23] -1.6010329132760395    [57] -0.09444777849909261  [191] -0.20224886388231217 \s
                 [24]           ?            [58]  0.30825813247521566  [192]           ?          \s
                 [25]  0.3697439093097257    [59] -1.4707970046267829   [193] -0.5294698561555315  \s
                 [26] -0.866674032117043     [60]           ?           [194]  1.71293218164024    \s
                 [27]  0.2575936498990307    [61] -2.005904482802051    [195] -1.2866207005261927  \s
                 [28]           ?            [62]  1.7896725865838126   [196]           ?          \s
                 [29] -0.5367294116283956    [63]  0.10741226589265612  [197]  1.1222908028593523  \s
                 [30]  0.6122595900292269    [64]           ?           [198] -0.038230386122934265\s
                 [31]  0.6283115778307867    [65]  0.15057809464818378  [199] -1.019025608752341   \s
                 [32]           ?            [66] -0.2627364006171766                              \s
                 [33] -0.0846767320121113    [67]  0.15946839026843737 \s
                """, x.toContent());

        assertEquals("""
                VarDouble [name:"?", rowCount:200]
                 row          value          row          value          row          value        \s
                  [0]           ?            [67]  0.15946839026843737  [134]  0.5067460766099371  \s
                  [1]  0.39028109726714816   [68]           ?           [135] -1.4055612385790348  \s
                  [2] -0.4969259911766421    [69] -0.8811788136295773   [136]           ?          \s
                  [3] -0.08458437057255112   [70]  0.536437541495931    [137] -1.3973117444533316  \s
                  [4]           ?            [71] -1.0112583879983437   [138] -0.24133234999416894 \s
                  [5]  1.1512637194768724    [72]           ?           [139] -1.4363241008765577  \s
                  [6]  0.2087159504897916    [73] -0.9710496821450608   [140]           ?          \s
                  [7]  0.1693097723155751    [74]  0.3620872239553609   [141]  0.09842712570379304 \s
                  [8]           ?            [75]  1.2479582661214295   [142]  1.353007540108721   \s
                  [9] -1.1062646787748283    [76]           ?           [143]  1.1810744441307794  \s
                 [10]  0.9310864075677463    [77]  0.4264658345148021   [144]           ?          \s
                 [11] -0.24737417781497056   [78]  1.4864832620223603   [145] -1.1702649932622096  \s
                 [12]           ?            [79] -1.0755159402428762   [146]  0.8495029966698171  \s
                 [13] -1.1560235891200092    [80]           ?           [147]  0.4583234725837861  \s
                 [14]  0.3677808539928047    [81]  0.655753487402481    [148]           ?          \s
                 [15]  0.04428390568642385   [82]  1.352953400799322    [149] -2.5426738029365583  \s
                 [16]           ?            [83]  0.4104371734069073   [150]  0.2437439724833285  \s
                 [17]  2.087138707363908     [84]           ?           [151] -0.19389857481402678 \s
                 [18] -0.6484483867259064    [85]  0.7376520388300956   [152]           ?          \s
                 [19] -0.20222328383409388   [86] -0.7766431685474735   [153] -1.0990664453417598  \s
                 [20]           ?            [87]  2.0067380267355026   [154]  0.8191411300241794  \s
                 [21] -1.082633410858406     [88]           ?           [155]  0.7662642869285238  \s
                 [22] -0.8585978973902683    [89]  0.46544129133784984  [156]           ?          \s
                 [23] -1.6010329132760395    [90] -1.451686906926788    [157] -0.2870929262099685  \s
                 [24]           ?            [91]  0.16788956807506228  [158] -0.824302525853852   \s
                 [25]  0.3697439093097257    [92]           ?           [159] -0.5484135494771385  \s
                 [26] -0.866674032117043     [93] -0.3532948234090509   [160]           ?          \s
                 [27]  0.2575936498990307    [94] -1.3440782040810393   [161]  0.06894440588924175 \s
                 [28]           ?            [95] -1.1434814266372637   [162]  0.31176825036506545 \s
                 [29] -0.5367294116283956    [96]           ?           [163] -0.28892224447693804 \s
                 [30]  0.6122595900292269    [97] -1.1405000212914576   [164]           ?          \s
                 [31]  0.6283115778307867    [98] -0.997431061422807    [165]  1.668460478431417   \s
                 [32]           ?            [99] -0.8355704199569222   [166]  0.05647386892154898 \s
                 [33] -0.0846767320121113   [100]           ?           [167] -1.164592525258854   \s
                 [34] -0.09362660880470101  [101] -0.22879598069497525  [168]           ?          \s
                 [35]  1.2676072196723072   [102] -0.3391072059428062   [169] -1.7678708260409146  \s
                 [36]           ?           [103]  1.043988545137548    [170] -0.3463239819323055  \s
                 [37] -0.35649914577043024  [104]           ?           [171] -0.049140028694945145\s
                 [38] -0.05105659432410365  [105]  1.1305554273391956   [172]           ?          \s
                 [39] -0.31284231287329395  [106]  0.8184613019208978   [173]  1.0411656369122644  \s
                 [40]           ?           [107] -1.327956641366741    [174]  0.9358435121394187  \s
                 [41] -0.45203587341319634  [108]           ?           [175] -0.3420647211250892  \s
                 [42] -0.4842270880516015   [109]  0.8183007210455703   [176]           ?          \s
                 [43]  0.5840719886385682   [110]  2.0939327960287217   [177] -0.1876595851132685  \s
                 [44]           ?           [111]  1.116119066864704    [178]  0.9617246803002835  \s
                 [45]  1.6808275328429871   [112]           ?           [179] -0.1411268764138725  \s
                 [46]  0.18171623874784112  [113]  0.3136267101441643   [180]           ?          \s
                 [47]  0.43797562020584396  [114] -0.537601980765285    [181]  1.4330590460348056  \s
                 [48]           ?           [115]  0.6442586071174642   [182]  0.21380165834928597 \s
                 [49]  1.2946902376585687   [116]           ?           [183]  0.10370093167281998 \s
                 [50]  2.3935302203873285   [117]  0.9488237505163257   [184]           ?          \s
                 [51]  1.8521159876697453   [118]  1.2261406743055818   [185] -0.6420141737488306  \s
                 [52]           ?           [119]  0.22952424252283166  [186]  0.12041419801228942 \s
                 [53] -1.1320113711661468   [120]           ?           [187] -0.5737849693973599  \s
                 [54]  0.5508956502480397   [121]  0.18338101865551507  [188]           ?          \s
                 [55]  1.4914125103602476   [122] -1.0371901131435757   [189] -1.930123005115381   \s
                 [56]           ?           [123]  0.8325190159657154   [190]  0.8269811051702728  \s
                 [57] -0.09444777849909261  [124]           ?           [191] -0.20224886388231217 \s
                 [58]  0.30825813247521566  [125]  1.0623079917947502   [192]           ?          \s
                 [59] -1.4707970046267829   [126] -0.3063071925210918   [193] -0.5294698561555315  \s
                 [60]           ?           [127] -0.5180611252859123   [194]  1.71293218164024    \s
                 [61] -2.005904482802051    [128]           ?           [195] -1.2866207005261927  \s
                 [62]  1.7896725865838126   [129]  1.5282007476934956   [196]           ?          \s
                 [63]  0.10741226589265612  [130]  1.2726592279419575   [197]  1.1222908028593523  \s
                 [64]           ?           [131]  0.2643650313120908   [198] -0.038230386122934265\s
                 [65]  0.15057809464818378  [132]           ?           [199] -1.019025608752341   \s
                 [66] -0.2627364006171766   [133]  1.06270324331314    \s
                """, x.toFullContent());
    }

    @Test
    void testSequence() {
        assertTrue(VarDouble.seq(0, 0.9, 0.3).deepEquals(VarDouble.from(4, r -> r * 0.3)));
        assertTrue(VarDouble.seq(-1, 1, 0.25).deepEquals(VarDouble.from(9, r -> r * 0.25 - 1)));
    }
}
