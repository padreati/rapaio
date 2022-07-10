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


import static org.junit.jupiter.api.Assertions.*;

import static rapaio.sys.With.*;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.stat.Mean;
import rapaio.sys.WS;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarBinaryTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(123);
    }

    @Test
    void testEmpty() {
        VarBinary b = VarBinary.empty();
        b.addInt(1);
        b.addInt(1);
        b.addInt(0);
        b.addMissing();
        b.addMissing();
        b.addInt(1);

        assertEquals(1, b.stream().complete().filter(s -> s.getInt() != 1).count());
        assertEquals(3, b.stream().complete().filter(s -> s.getInt() == 1).count());
        assertEquals(2, b.stream().incomplete().count());

        assertEquals(10, VarBinary.empty(10).stream().incomplete().count());
        assertEquals(0, VarBinary.empty().stream().incomplete().count());
    }

    @Test
    void testFill() {
        VarBinary b = VarBinary.fill(10, 0);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(10, b.stream().complete().filter(s -> s.getInt() != 1).count());
        assertEquals(0, b.stream().complete().filter(s -> s.getInt() == 1).count());

        b = VarBinary.fill(10, 1);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(0, b.stream().complete().filter(s -> s.getInt() != 1).count());
        assertEquals(10, b.stream().complete().filter(s -> s.getInt() == 1).count());
    }

    @Test
    public void testNumericStats() {
        VarBinary b = VarBinary.copy(1, 1, 0, 0, 1, 0, 1, 1);
        assertEquals(0.625, Mean.of(b).value(), 10e-10);
    }

    @Test
    void testMissingValues() {
        VarBinary bin = VarBinary.copy(1, 0, 1, 0, -1, -1, 1, 0);
        assertEquals(8, bin.size());
        assertTrue(bin.isMissing(4));
        assertFalse(bin.isMissing(7));

        bin = VarBinary.empty();
        bin.addMissing();
        bin.addInt(1);
        bin.setMissing(1);

        assertEquals(2, bin.size());
        assertTrue(bin.isMissing(0));
        assertTrue(bin.isMissing(1));
    }

    @Test
    void testBuilders() {
        VarBinary bin = VarBinary.copy(1, 1, 0, 0);
        assertEquals(4, bin.size());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(3));

        VarBinary bin2 = VarBinary.fromIndex(100, i -> i % 3 == 0 ? -1 : i % 3 == 1 ? 0 : 1);
        for (int i = 0; i < bin2.size(); i++) {
            switch (i % 3) {
                case 0 -> assertTrue(bin2.isMissing(i));
                case 1 -> assertEquals(0, bin2.getInt(i));
                default -> assertEquals(1, bin2.getInt(i));
            }
        }

        Boolean[] array = new Boolean[100];
        for (int i = 0; i < array.length; i++) {
            switch (i % 3) {
                case 0 -> array[i] = null;
                case 1 -> array[i] = false;
                default -> array[i] = true;
            }
        }

        VarBinary bin4 = VarBinary.from(100, row -> array[row]);
        assertTrue(bin2.deepEquals(bin4));

        assertTrue(VarBinary.empty(10).deepEquals(bin4.newInstance(10)));
    }

    @Test
    void testOther() {
        VarBinary bin = VarBinary.empty();
        bin.addDouble(1);
        bin.setDouble(0, 0);
        bin.addInt(1);
        bin.setInt(1, 0);

        assertEquals(0, bin.getDouble(0), 10e-10);
        assertEquals(0, bin.getInt(1));

        VarBinary copy = bin.copy();
        assertEquals(0, copy.getInt(0));
        assertEquals(0, copy.getInt(1));
        assertEquals(2, copy.size());

        copy.removeRow(0);
        assertEquals(1, copy.size());
        assertEquals(0, copy.getInt(0));

        copy.clearRows();
        assertEquals(0, copy.size());

        copy.removeRow(10);

        VarBinary bin1 = VarBinary.fill(10, 1);
        bin1.addRows(10);
        assertEquals(20, bin1.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(1, bin1.getInt(i));
            assertTrue(bin1.isMissing(i + 10));
        }
    }

    @Test
    void testDouble() {

        VarBinary bin = VarBinary.empty();
        bin.addDouble(1);
        bin.addDouble(0);
        bin.addDouble(-1);

        assertEquals(3, bin.size());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));

        bin.setDouble(0, -1);
        bin.setDouble(1, 0);
        bin.setDouble(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));
    }

    @Test
    void testInt() {

        VarBinary bin = VarBinary.empty();
        bin.addInt(1);
        bin.addInt(0);
        bin.addInt(-1);

        assertEquals(3, bin.size());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));
        assertEquals(VarInt.MISSING_VALUE, bin.getInt(2));

        bin.setInt(0, -1);
        bin.setInt(1, 0);
        bin.setInt(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));
    }

    @Test
    void testLong() {

        VarBinary bin = VarBinary.empty();
        bin.addLong(1);
        bin.addLong(0);
        bin.addLong(-1);

        assertEquals(3, bin.size());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));

        bin.setLong(0, -1);
        bin.setLong(1, 0);
        bin.setLong(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));

        assertEquals(VarLong.MISSING_VALUE, bin.getLong(0));
    }

    @Test
    void testAddInvalidLabel() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> VarBinary.empty().addLabel("x"));
        assertEquals("The value x could not be converted to a binary value", ex.getMessage());
    }

    @Test
    void testSetInvalidLabel() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> VarBinary.empty(0).setLabel(0, "x"));
        assertEquals("The value x could not be converted to a binary value", ex.getMessage());
    }

    @Test
    void testLabel() {

        String[] labels = new String[] {"1", "0", "1", "?"};

        VarBinary bin = VarBinary.empty();
        for (String label : labels) {
            bin.addLabel(label);
        }

        assertEquals(labels.length, bin.size());
        for (int i = 0; i < labels.length; i++) {
            assertEquals(labels[i], bin.getLabel(i));
        }

        for (int i = 0; i < labels.length; i++) {
            bin.setLabel(i, labels[i]);
        }

        for (int i = 0; i < labels.length; i++) {
            assertEquals(labels[i], bin.getLabel(i));
        }

        List<String> levels = VarBinary.empty().levels();
        assertEquals(3, levels.size());
        assertEquals("?", levels.get(0));
        assertEquals("true", levels.get(1));
        assertEquals("false", levels.get(2));
    }

    @Test
    void testIllegalSetLevels() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> VarBinary.empty().setLevels("?", "1"));
        assertEquals("Operation not implemented on binary variables", ex.getMessage());
    }

    @Test
    void testPrint() {
        VarBinary var = VarBinary.copy(IntStream.range(0, 200).map(x -> random.nextInt(3) - 1).toArray()).name("x");
        assertEquals("VarBinary [name:\"x\", rowCount:200, values: 1, 1, 1, 1, ?, 1, ?, 0, 1, 0, 1, ?, 1, 1, 0, ?, ..., 0, 0]",
                var.toString());

        WS.getPrinter().withOptions(textWidth(100));

        assertEquals("""
                        VarBinary [name:"x", rowCount:200]
                         row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value\s
                          [0]   1    [11]   ?    [22]   ?    [33]   ?    [44]   1    [55]   ?    [66]   ?    [77]   0   [188]   ?   [199]   0  \s
                          [1]   1    [12]   1    [23]   1    [34]   ?    [45]   0    [56]   0    [67]   0    [78]   0   [189]   ?              \s
                          [2]   1    [13]   1    [24]   1    [35]   ?    [46]   0    [57]   1    [68]   1    ...   ...  [190]   1  \s
                          [3]   1    [14]   0    [25]   1    [36]   0    [47]   ?    [58]   ?    [69]   1   [180]   ?   [191]   ?  \s
                          [4]   ?    [15]   ?    [26]   0    [37]   ?    [48]   0    [59]   0    [70]   0   [181]   ?   [192]   ?  \s
                          [5]   1    [16]   1    [27]   0    [38]   1    [49]   0    [60]   0    [71]   0   [182]   ?   [193]   0  \s
                          [6]   ?    [17]   1    [28]   0    [39]   0    [50]   ?    [61]   ?    [72]   ?   [183]   0   [194]   ?  \s
                          [7]   0    [18]   ?    [29]   0    [40]   0    [51]   0    [62]   1    [73]   0   [184]   0   [195]   ?  \s
                          [8]   1    [19]   ?    [30]   0    [41]   1    [52]   0    [63]   ?    [74]   ?   [185]   ?   [196]   1  \s
                          [9]   0    [20]   1    [31]   1    [42]   ?    [53]   1    [64]   1    [75]   ?   [186]   1   [197]   ?  \s
                         [10]   1    [21]   ?    [32]   ?    [43]   0    [54]   ?    [65]   1    [76]   0   [187]   0   [198]   0  \s
                        """,
                var.toContent());

        assertEquals("""
                        VarBinary [name:"x", rowCount:200]
                         row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value\s
                          [0]   1    [20]   1    [40]   0    [60]   0    [80]   ?   [100]   1   [120]   1   [140]   1   [160]   ?   [180]   ?  \s
                          [1]   1    [21]   ?    [41]   1    [61]   ?    [81]   1   [101]   ?   [121]   0   [141]   0   [161]   0   [181]   ?  \s
                          [2]   1    [22]   ?    [42]   ?    [62]   1    [82]   0   [102]   0   [122]   0   [142]   1   [162]   ?   [182]   ?  \s
                          [3]   1    [23]   1    [43]   0    [63]   ?    [83]   ?   [103]   1   [123]   0   [143]   1   [163]   1   [183]   0  \s
                          [4]   ?    [24]   1    [44]   1    [64]   1    [84]   ?   [104]   0   [124]   1   [144]   1   [164]   1   [184]   0  \s
                          [5]   1    [25]   1    [45]   0    [65]   1    [85]   0   [105]   1   [125]   ?   [145]   0   [165]   0   [185]   ?  \s
                          [6]   ?    [26]   0    [46]   0    [66]   ?    [86]   1   [106]   1   [126]   0   [146]   0   [166]   ?   [186]   1  \s
                          [7]   0    [27]   0    [47]   ?    [67]   0    [87]   0   [107]   1   [127]   0   [147]   1   [167]   1   [187]   0  \s
                          [8]   1    [28]   0    [48]   0    [68]   1    [88]   ?   [108]   ?   [128]   0   [148]   1   [168]   ?   [188]   ?  \s
                          [9]   0    [29]   0    [49]   0    [69]   1    [89]   0   [109]   ?   [129]   ?   [149]   ?   [169]   1   [189]   ?  \s
                         [10]   1    [30]   0    [50]   ?    [70]   0    [90]   ?   [110]   ?   [130]   0   [150]   1   [170]   0   [190]   1  \s
                         [11]   ?    [31]   1    [51]   0    [71]   0    [91]   0   [111]   ?   [131]   1   [151]   ?   [171]   ?   [191]   ?  \s
                         [12]   1    [32]   ?    [52]   0    [72]   ?    [92]   0   [112]   1   [132]   1   [152]   ?   [172]   ?   [192]   ?  \s
                         [13]   1    [33]   ?    [53]   1    [73]   0    [93]   0   [113]   1   [133]   ?   [153]   ?   [173]   ?   [193]   0  \s
                         [14]   0    [34]   ?    [54]   ?    [74]   ?    [94]   ?   [114]   1   [134]   1   [154]   ?   [174]   ?   [194]   ?  \s
                         [15]   ?    [35]   ?    [55]   ?    [75]   ?    [95]   ?   [115]   1   [135]   0   [155]   0   [175]   0   [195]   ?  \s
                         [16]   1    [36]   0    [56]   0    [76]   0    [96]   0   [116]   0   [136]   ?   [156]   0   [176]   1   [196]   1  \s
                         [17]   1    [37]   ?    [57]   1    [77]   0    [97]   1   [117]   ?   [137]   1   [157]   ?   [177]   1   [197]   ?  \s
                         [18]   ?    [38]   1    [58]   ?    [78]   0    [98]   ?   [118]   1   [138]   0   [158]   ?   [178]   1   [198]   0  \s
                         [19]   ?    [39]   0    [59]   0    [79]   1    [99]   0   [119]   ?   [139]   ?   [159]   1   [179]   1   [199]   0  \s
                        """,
                var.toFullContent());

    }
}
