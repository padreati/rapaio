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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.*;
import rapaio.core.stat.Mean;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class VarBinaryTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testEmpty() {
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
    public void testFill() {
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
        b.printSummary();
        assertEquals(0.625, Mean.of(b).value(), 10e-10);
    }

    @Test
    public void testMissingValues() {
        VarBinary bin = VarBinary.copy(1, 0, 1, 0, -1, -1, 1, 0);
        assertEquals(8, bin.rowCount());
        assertTrue(bin.isMissing(4));
        assertFalse(bin.isMissing(7));

        bin = VarBinary.empty();
        bin.addMissing();
        bin.addInt(1);
        bin.setMissing(1);

        assertEquals(2, bin.rowCount());
        assertTrue(bin.isMissing(0));
        assertTrue(bin.isMissing(1));
    }

    @Test
    public void testBuilders() {
        VarBinary bin = VarBinary.copy(1, 1, 0, 0);
        assertEquals(4, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(3));

        VarBinary bin2 = VarBinary.fromIndex(100, i -> i % 3 == 0 ? -1 : i % 3 == 1 ? 0 : 1);
        for (int i = 0; i < bin2.rowCount(); i++) {
            switch (i % 3) {
                case 0:
                    assertTrue(bin2.isMissing(i));
                    break;
                case 1:
                    assertEquals(0, bin2.getInt(i));
                    break;
                default:
                    assertEquals(1, bin2.getInt(i));
            }
        }

        Boolean[] array = new Boolean[100];
        for (int i = 0; i < array.length; i++) {
            switch (i % 3) {
                case 0:
                    array[i] = null;
                    break;
                case 1:
                    array[i] = false;
                    break;
                default:
                    array[i] = true;
            }
        }

        VarBinary bin4 = VarBinary.from(100, row -> array[row]);
        assertTrue(bin2.deepEquals(bin4));

        assertTrue(VarBinary.empty(10).deepEquals(bin4.newInstance(10)));
    }

    @Test
    public void testOther() {
        VarBinary bin = VarBinary.empty();
        bin.addDouble(1);
        bin.setDouble(0, 0);
        bin.addInt(1);
        bin.setInt(1, 0);

        assertEquals(0, bin.getDouble(0), 10e-10);
        assertEquals(0, bin.getInt(1));

        VarBinary copy = bin.solidCopy();
        assertEquals(0, copy.getInt(0));
        assertEquals(0, copy.getInt(1));
        assertEquals(2, copy.rowCount());

        copy.removeRow(0);
        assertEquals(1, copy.rowCount());
        assertEquals(0, copy.getInt(0));

        copy.clearRows();
        assertEquals(0, copy.rowCount());

        copy.removeRow(10);

        VarBinary bin1 = VarBinary.fill(10, 1);
        bin1.addRows(10);
        assertEquals(20, bin1.rowCount());
        for (int i = 0; i < 10; i++) {
            assertEquals(1, bin1.getInt(i));
            assertTrue(bin1.isMissing(i + 10));
        }
    }

    @Test
    public void testDouble() {

        VarBinary bin = VarBinary.empty();
        bin.addDouble(1);
        bin.addDouble(0);
        bin.addDouble(-1);

        assertEquals(3, bin.rowCount());
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
    public void testInt() {

        VarBinary bin = VarBinary.empty();
        bin.addInt(1);
        bin.addInt(0);
        bin.addInt(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));
        assertEquals(Integer.MIN_VALUE, bin.getInt(2));

        bin.setInt(0, -1);
        bin.setInt(1, 0);
        bin.setInt(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));
    }

    @Test
    public void testLong() {

        VarBinary bin = VarBinary.empty();
        bin.addLong(1);
        bin.addLong(0);
        bin.addLong(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));

        bin.setLong(0, -1);
        bin.setLong(1, 0);
        bin.setLong(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));

        assertEquals(Long.MIN_VALUE, bin.getLong(0));
    }

    @Test
    public void testAddInvalidLabel() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The value x could not be converted to a binary value");
        VarBinary.empty().addLabel("x");
    }

    @Test
    public void testSetInvalidLabel() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The value x could not be converted to a binary value");
        VarBinary.empty(0).setLabel(0, "x");
    }

    @Test
    public void testLabel() {

        String[] labels = new String[]{"1", "0", "1", "?"};

        VarBinary bin = VarBinary.empty();
        for (String label : labels) {
            bin.addLabel(label);
        }

        assertEquals(labels.length, bin.rowCount());
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
    public void testIllegalSetLevels() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Operation not implemented on binary variables");
        VarBinary.empty().setLevels("?", "1");
    }

    @Test
    public void testPrint() {
        VarBinary var = VarBinary.copy(IntStream.range(0, 200).map(x -> RandomSource.nextInt(3) - 1).toArray()).withName("x");
        assertEquals("VarBinary [name: \"x\", rowCount: 200, values: 1, 1, 1, 1, ?, 1, ?, 0, 1, 0, 1, ?, 1, 1, 0, ?, ..., 0, 0]", var.toString());

        assertEquals("VarBinary [name: \"x\", rowCount: 200]\n" +
                " row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value \n" +
                "  [0]   1    [11]   ?    [22]   ?    [33]   ?    [44]   1    [55]   ?    [66]   ?    [77]   0   [188]   ?   [199]   0   \n" +
                "  [1]   1    [12]   1    [23]   1    [34]   ?    [45]   0    [56]   0    [67]   0    [78]   0   [189]   ?               \n" +
                "  [2]   1    [13]   1    [24]   1    [35]   ?    [46]   0    [57]   1    [68]   1     ...  ...  [190]   1   \n" +
                "  [3]   1    [14]   0    [25]   1    [36]   0    [47]   ?    [58]   ?    [69]   1   [180]   ?   [191]   ?   \n" +
                "  [4]   ?    [15]   ?    [26]   0    [37]   ?    [48]   0    [59]   0    [70]   0   [181]   ?   [192]   ?   \n" +
                "  [5]   1    [16]   1    [27]   0    [38]   1    [49]   0    [60]   0    [71]   0   [182]   ?   [193]   0   \n" +
                "  [6]   ?    [17]   1    [28]   0    [39]   0    [50]   ?    [61]   ?    [72]   ?   [183]   0   [194]   ?   \n" +
                "  [7]   0    [18]   ?    [29]   0    [40]   0    [51]   0    [62]   1    [73]   0   [184]   0   [195]   ?   \n" +
                "  [8]   1    [19]   ?    [30]   0    [41]   1    [52]   0    [63]   ?    [74]   ?   [185]   ?   [196]   1   \n" +
                "  [9]   0    [20]   1    [31]   1    [42]   ?    [53]   1    [64]   1    [75]   ?   [186]   1   [197]   ?   \n" +
                " [10]   1    [21]   ?    [32]   ?    [43]   0    [54]   ?    [65]   1    [76]   0   [187]   0   [198]   0   \n", var.content());

        assertEquals("VarBinary [name: \"x\", rowCount: 200]\n" +
                " row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value  row  value \n" +
                "  [0]   1    [15]   ?    [30]   0    [45]   0    [60]   0    [75]   ?    [90]   ?   [105]   1   [120]   1   [135]   0   [150]   1   [165]   0   [180]   ?   [195]   ?   \n" +
                "  [1]   1    [16]   1    [31]   1    [46]   0    [61]   ?    [76]   0    [91]   0   [106]   1   [121]   0   [136]   ?   [151]   ?   [166]   ?   [181]   ?   [196]   1   \n" +
                "  [2]   1    [17]   1    [32]   ?    [47]   ?    [62]   1    [77]   0    [92]   0   [107]   1   [122]   0   [137]   1   [152]   ?   [167]   1   [182]   ?   [197]   ?   \n" +
                "  [3]   1    [18]   ?    [33]   ?    [48]   0    [63]   ?    [78]   0    [93]   0   [108]   ?   [123]   0   [138]   0   [153]   ?   [168]   ?   [183]   0   [198]   0   \n" +
                "  [4]   ?    [19]   ?    [34]   ?    [49]   0    [64]   1    [79]   1    [94]   ?   [109]   ?   [124]   1   [139]   ?   [154]   ?   [169]   1   [184]   0   [199]   0   \n" +
                "  [5]   1    [20]   1    [35]   ?    [50]   ?    [65]   1    [80]   ?    [95]   ?   [110]   ?   [125]   ?   [140]   1   [155]   0   [170]   0   [185]   ?   \n" +
                "  [6]   ?    [21]   ?    [36]   0    [51]   0    [66]   ?    [81]   1    [96]   0   [111]   ?   [126]   0   [141]   0   [156]   0   [171]   ?   [186]   1   \n" +
                "  [7]   0    [22]   ?    [37]   ?    [52]   0    [67]   0    [82]   0    [97]   1   [112]   1   [127]   0   [142]   1   [157]   ?   [172]   ?   [187]   0   \n" +
                "  [8]   1    [23]   1    [38]   1    [53]   1    [68]   1    [83]   ?    [98]   ?   [113]   1   [128]   0   [143]   1   [158]   ?   [173]   ?   [188]   ?   \n" +
                "  [9]   0    [24]   1    [39]   0    [54]   ?    [69]   1    [84]   ?    [99]   0   [114]   1   [129]   ?   [144]   1   [159]   1   [174]   ?   [189]   ?   \n" +
                " [10]   1    [25]   1    [40]   0    [55]   ?    [70]   0    [85]   0   [100]   1   [115]   1   [130]   0   [145]   0   [160]   ?   [175]   0   [190]   1   \n" +
                " [11]   ?    [26]   0    [41]   1    [56]   0    [71]   0    [86]   1   [101]   ?   [116]   0   [131]   1   [146]   0   [161]   0   [176]   1   [191]   ?   \n" +
                " [12]   1    [27]   0    [42]   ?    [57]   1    [72]   ?    [87]   0   [102]   0   [117]   ?   [132]   1   [147]   1   [162]   ?   [177]   1   [192]   ?   \n" +
                " [13]   1    [28]   0    [43]   0    [58]   ?    [73]   0    [88]   ?   [103]   1   [118]   1   [133]   ?   [148]   1   [163]   1   [178]   1   [193]   0   \n" +
                " [14]   0    [29]   0    [44]   1    [59]   0    [74]   ?    [89]   0   [104]   0   [119]   ?   [134]   1   [149]   ?   [164]   1   [179]   1   [194]   ?   \n", var.fullContent());

    }
}
