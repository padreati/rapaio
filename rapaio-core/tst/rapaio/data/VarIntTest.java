/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import static rapaio.printer.opt.POpts.textWidth;

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import rapaio.core.stat.Sum;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarIntTest {

    private static final double TOL = 1e-20;

    @Test
    void smokeTest() {
        Var index = VarInt.empty(1);
        assertTrue(index.type().isNumeric());
        assertFalse(index.type().isNominal());
        assertEquals(1, index.size());
    }

    @Test
    void invalidRowNumber() {
        var ex = assertThrows(IllegalArgumentException.class, () -> VarInt.empty(-1));
        assertEquals("Illegal row count: -1", ex.getMessage());
    }

    @Test
    void unparsableSetLabel() {
        var ex = assertThrows(NumberFormatException.class, () -> VarInt.empty(1).setLabel(0, "Test"));
        assertEquals("For input string: \"Test\"", ex.getMessage());
    }

    @Test
    void unparsableAddLabel() {
        var ex = assertThrows(NumberFormatException.class, () -> VarInt.empty(1).addLabel("Test"));
        assertEquals("For input string: \"Test\"", ex.getMessage());
    }

    @Test
    void testNotImplementedLevels() {
        var ex = assertThrows(IllegalStateException.class, () -> VarInt.seq(10).levels());
        assertEquals("Operation not available for integer variables.", ex.getMessage());
    }

    @Test
    void testNotImplementedSetLevels() {
        var ex = assertThrows(IllegalStateException.class, () -> VarInt.seq(10).setLevels(new String[] {"a", "b"}));
        assertEquals("Operation not available for integer variables.", ex.getMessage());
    }

    @Test
    void testSetterGetter() {

        Var index = VarInt.fill(3, 0);

        assertEquals(0, index.getInt(0));
        index.setInt(0, 1);
        index.setInt(1, 3);

        assertEquals(1, index.getInt(0));
        assertEquals(3, index.getInt(1));

        assertEquals(1., index.getDouble(0), 1e-10);
        assertEquals(3., index.getDouble(1), 1e-10);

        index.setDouble(0, 2.5);
        index.setDouble(1, 7.8);
        index.setDouble(2, 2.51);

        assertEquals(2, index.getInt(0));
        assertEquals(2., index.getDouble(0), 1e-10);
        assertEquals(8, index.getInt(1));
        assertEquals(8., index.getDouble(1), 1e-10);
        assertEquals(3, index.getInt(2));
        assertEquals(3., index.getDouble(2), 1e-10);
    }

    @Test
    void testMissing() {
        Var index = VarInt.seq(1, 10, 1);
        for (int i = 0; i < index.size(); i++) {
            assertFalse(index.isMissing(i));
        }
        for (int i = 0; i < index.size(); i++) {
            if (i % 2 == 0) {
                index.setMissing(i);
            }
        }
        for (int i = 0; i < index.size(); i++) {
            assertEquals(i % 2 == 0, index.isMissing(i));
        }
    }

    @Test
    void testOneIndex() {
        Var one = VarInt.scalar(2);
        assertEquals(1, one.size());
        assertEquals(2, one.getInt(0));

        one = VarInt.scalar(3);
        assertEquals(1, one.size());
        assertEquals(3, one.getInt(0));

        one.addRows(2);
        one.setInt(2, 10);
        assertEquals(3, one.size());
        assertEquals(10, one.getInt(2));

        one.setLabel(0, "?");
        assertTrue(one.isMissing(0));
    }

    @Test
    void testBuilders() {
        Var empty1 = VarInt.empty();
        assertEquals(0, empty1.size());
        empty1 = VarInt.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(VarInt.MISSING_VALUE, empty1.getInt(i));
        }

        Var fill1 = VarInt.fill(10, -1);
        assertEquals(10, fill1.size());
        for (int i = 0; i < fill1.size(); i++) {
            assertEquals(-1, fill1.getInt(i));
        }

        Var seq1 = VarInt.seq(1, 10);
        assertEquals(10, seq1.size());
        for (int i = 0; i < seq1.size(); i++) {
            assertEquals(i + 1, seq1.getInt(i));
        }

        int[] src = new int[] {1, 2, 3, 4};
        VarInt x1 = VarInt.copy(src);
        VarInt x2 = VarInt.wrap(src);
        VarInt x3 = VarInt.seq(4);
        VarInt x4 = VarInt.seq(1, 4);
        VarInt x5 = VarInt.seq(1, 4, 2);
        VarInt x6 = VarInt.empty();
        x6.addInt(1);
        x6.addInt(2);
        x6.addInt(3);
        x6.addInt(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.getInt(i));
            assertEquals(i + 1, x2.getInt(i));
            assertEquals(i, x3.getInt(i));
            assertEquals(i + 1, x4.getInt(i));
            assertEquals(i * 2 + 1, x5.getInt(i));
            assertEquals(i + 1, x6.getInt(i));
        }

        src[2] = 10;
        assertEquals(10, x2.getInt(2));

        VarInt from1 = VarInt.from(x2.size(), x2::getInt);
        assertTrue(from1.deepEquals(x2));

        VarInt collect1 = IntStream.range(0, 100).boxed().collect(VarInt.collector());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, collect1.getInt(i));
        }
        VarInt collect2 = IntStream.range(0, 100).boxed().parallel().collect(VarInt.collector());
        int sum = (int) Sum.of(collect2).value();
        assertEquals(99 * 100 / 2, sum);

        VarInt empty3 = collect2.newInstance(10);
        VarInt empty4 = VarInt.empty(10);
        assertTrue(empty3.deepEquals(empty4));
    }

    @Test
    void testLabels() {
        int[] array = new int[] {1, 2, 3, VarInt.MISSING_VALUE, 5, 6, VarInt.MISSING_VALUE};

        VarInt int1 = VarInt.empty();
        for (int val : array) {
            int1.addInt(val);
        }
        for (int i = 0; i < int1.size(); i++) {
            if (array[i] == VarInt.MISSING_VALUE) {
                assertEquals(VarNominal.MISSING_VALUE, int1.getLabel(i));
            } else {
                assertEquals(String.valueOf(i + 1), int1.getLabel(i));
            }
        }

        VarInt int2 = VarInt.copy(1, 2, 3);
        int2.addLabel("10");

        assertEquals(4, int2.size());
        assertEquals("1", int2.getLabel(0));
        assertEquals("2", int2.getLabel(1));
        assertEquals("3", int2.getLabel(2));
        assertEquals("10", int2.getLabel(3));

        VarInt x3 = VarInt.copy(1, 2, 3);
        x3.setLabel(0, "10");
        x3.removeRow(1);

        assertEquals(2, x3.size());
        assertEquals("10", x3.getLabel(0));
        assertEquals("3", x3.getLabel(1));

        String[] stringValues = new String[] {"?", "-4", "4", "?"};
        VarInt x4 = VarInt.empty();
        for (String str : stringValues) {
            x4.addLabel(str);
        }

        assertEquals(stringValues.length, x4.size());
        for (int i = 0; i < stringValues.length; i++) {
            assertEquals(stringValues[i], x4.getLabel(i));
        }
    }

    @Test
    void testDouble() {

        double[] values = new double[] {0, 1, Double.NaN, 3, 4, Double.NaN, 6, 7, -8, -100};
        VarInt int1 = VarInt.empty();
        for (double val : values) {
            int1.addDouble(val);
        }

        assertEquals(values.length, int1.size());
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) {
                assertTrue(int1.isMissing(i));
            } else {
                assertEquals(values[i], int1.getDouble(i), TOL);
            }
        }

        for (int i = 0; i < int1.size(); i++) {
            int1.setDouble(i, values[i]);
        }
        assertEquals(values.length, int1.size());
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) {
                assertTrue(int1.isMissing(i));
                assertEquals(Double.NaN, int1.getDouble(i), TOL);
            } else {
                assertEquals(values[i], int1.getDouble(i), TOL);
            }
        }
    }

    @Test
    void testStamp() {
        VarInt x = VarInt.empty();
        x.addLong(0);
        x.addMissing();
        x.setLong(1, 100);

        assertEquals(0, x.getLong(0));
        assertEquals(100, x.getLong(1));
    }

    @Test
    public void testRemoveClear() {

        VarInt x = VarInt.copy(1, 3, 6, 7, 9);
        x.removeRow(0);

        assertEquals(4, x.size());
        assertEquals(3, x.getInt(0));
        assertEquals(9, x.getInt(3));

        x.clearRows();
        assertEquals(0, x.size());

        assertThrows(IndexOutOfBoundsException.class, () -> x.removeRow(-1));
    }

    @Test
    void testSolidCopy() {

        VarInt x1 = VarInt.copy(1, 2, 3, 4, 5);
        Var x2 = MappedVar.byRows(x1, 0, 1, 2);
        Var x3 = x2.copy();
        Var x4 = x3.copy();
        x4.addDouble(8);

        assertEquals(4, x4.size());
        assertEquals(1, x4.getInt(0));
        assertEquals(3, x4.getInt(2));
        assertEquals(8, x4.getInt(3));
    }

    @Test
    void testDataAccessor() {
        VarInt int1 = VarInt.seq(0, 100, 2);
        for (int i = 0; i < int1.size(); i++) {
            assertEquals(int1.getInt(i), int1.elements()[i]);
        }
        int[] values = new int[] {0, 1, Integer.MIN_VALUE, 3, 4};
        int1.setElements(values, values.length);

        assertTrue(VarInt.wrap(values).deepEquals(int1));
    }

    @Test
    void testString() {
        final VarInt x = VarInt.wrap(1, 2, VarInt.MISSING_VALUE,
                -10, 0, 100, VarInt.MISSING_VALUE, 16, 1, 2, 3, 4, 5, 6,
                7, 34, 322342, 2424, 24324, 24, 234234, 2423, 4, 234, 23,
                4, 2, 4, 23, 4, 23, 4, 234, 23, 423, 42, 34, 23);
        assertEquals("VarInt [name:\"?\", rowCount:38, values: 1, 2, ?, -10, 0, 100, ?, 16, 1, 2, 3, 4, ..., 34, 23]", x.toString());

        Random random = new Random(123);
        for (int i = 0; i < 200; i++) {
            x.addInt(random.nextInt(1000));
        }

        assertEquals("""
                VarInt [name:"?", rowCount:238]
                 row  value   row  value   row  value   row  value   row  value   row  value   row  value   row  value   row  value \s
                  [0]      1  [27]      4  [54]    420  [81]    950 [108]    488 [135]    899 [162]    755 [189]    974 [216]    792\s
                  [1]      2  [28]     23  [55]    585  [82]     14 [109]    549 [136]    371 [163]    381 [190]    432 [217]    311\s
                  [2]      ?  [29]      4  [56]    154  [83]    236 [110]    714 [137]    589 [164]    377 [191]    699 [218]    514\s
                  [3]    -10  [30]     23  [57]    416  [84]    443 [111]    775 [138]    420 [165]    576 [192]    387 [219]    202\s
                  [4]      0  [31]      4  [58]    922  [85]    850 [112]    826 [139]    110 [166]    235 [193]    417 [220]    259\s
                  [5]    100  [32]    234  [59]    837  [86]    768 [113]    993 [140]    858 [167]    958 [194]    480 [221]     53\s
                  [6]      ?  [33]     23  [60]    636  [87]    890 [114]    865 [141]    660 [168]    318 [195]    151 [222]    719\s
                  [7]     16  [34]    423  [61]    850  [88]    706 [115]     76 [142]    927 [169]    986 [196]    549 [223]    924\s
                  [8]      1  [35]     42  [62]    778  [89]    314 [116]    187 [143]    127 [170]    536 [197]    909 [224]    467\s
                  [9]      2  [36]     34  [63]    853  [90]    550 [117]    550 [144]    800 [171]    433 [198]    336 [225]     47\s
                 [10]      3  [37]     23  [64]    408  [91]    554 [118]    645 [145]    436 [172]    241 [199]    613 [226]    264\s
                 [11]      4  [38]    782  [65]    892  [92]    333 [119]    805 [146]    582 [173]    121 [200]    851 [227]    369\s
                 [12]      5  [39]    450  [66]    521  [93]    821 [120]    595 [147]    744 [174]    895 [201]    949 [228]    401\s
                 [13]      6  [40]    176  [67]    244  [94]    172 [121]    689 [148]    920 [175]    736 [202]    721 [229]    904\s
                 [14]      7  [41]    789  [68]     61  [95]    198 [122]    789 [149]    950 [176]    849 [203]    161 [230]    179\s
                 [15]     34  [42]    795  [69]    972  [96]     51 [123]    183 [150]    926 [177]      2 [204]    426 [231]    379\s
                 [16] 322342  [43]    657  [70]    330  [97]    117 [124]    572 [151]      4 [178]    113 [205]    137 [232]    959\s
                 [17]   2424  [44]    834  [71]    533  [98]    339 [125]    226 [152]    576 [179]    789 [206]    302 [233]    347\s
                 [18]  24324  [45]    837  [72]    452  [99]    843 [126]    731 [153]    688 [180]    368 [207]    437 [234]    699\s
                 [19]     24  [46]    585  [73]    375 [100]    729 [127]    984 [154]    915 [181]    741 [208]    474 [235]    639\s
                 [20] 234234  [47]    453  [74]    153 [101]    800 [128]    555 [155]     43 [182]     79 [209]    711 [236]    814\s
                 [21]   2423  [48]    639  [75]    834 [102]    397 [129]    324 [156]     94 [183]    581 [210]    653 [237]    688\s
                 [22]      4  [49]    726  [76]    535 [103]    798 [130]    301 [157]    452 [184]    206 [211]    191\s
                 [23]    234  [50]    472  [77]    186 [104]    518 [131]    356 [158]    219 [185]    132 [212]    741\s
                 [24]     23  [51]    565  [78]    748 [105]    827 [132]    753 [159]    921 [186]    619 [213]    369\s
                 [25]      4  [52]    637  [79]    842 [106]    720 [133]    805 [160]    132 [187]    903 [214]    724\s
                 [26]      2  [53]    749  [80]    643 [107]     90 [134]    137 [161]    347 [188]      7 [215]    307\s
                """, x.toFullContent(textWidth(100)));
    }
}
