/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.core.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.printer.opt.POpts.textWidth;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

public class HistogramTableTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testBuilders() {
        Tensor<Double> vector = Tensors.random(Shape.of(10_000), random);
        VarDouble variable = vector.dv().name("x");

        HistogramTable ht2 = new HistogramTable(variable, 0.1, 0.9, 20);
        for (double value : ht2.freq()) {
            assertTrue(value > 100);
            assertTrue(value < 200);
        }
        int count = 0;
        for(double value : vector) {
            if(value>=ht2.min() && value<=ht2.max()) {
                count++;
            }
        }
        assertEquals(count, ht2.freq().sum());
    }

    @Test
    void testPrinter() {
        Normal normal = Normal.std();
        VarDouble vector = VarDouble.from(10_000, () -> normal.sampleNext(random));
        HistogramTable ht = new HistogramTable(vector, 0, 1, 300);

        assertEquals("HistogramTable{min=0,max=1,bins=300,freq=[5.0,15.0,6.0,14.0,11.0,19.0,9.0,14.0,12.0,11.0,23.0,15.0,...]}",
                ht.toString());

        assertEquals("""
                HistogramTable
                ==============
                min=0
                max=1
                bins=300
                freq=[
                  [0]  5    [6]  9   [12] 16   [18] 13 \s
                  [1] 15    [7] 14   [13] 16   [19] 15 \s
                  [2]  6    [8] 12   [14] 17   ...  ...\s
                  [3] 14    [9] 11   [15] 12  [298]  4 \s
                  [4] 11   [10] 23   [16]  9  [299]  6 \s
                  [5] 19   [11] 15   [17] 11 \s
                ]}""", ht.toSummary());

        assertEquals("""
                HistogramTable
                ==============
                min=0
                max=1
                bins=300
                freq=[
                  [0]  5    [6]  9   [12] 16   [18] 13 \s
                  [1] 15    [7] 14   [13] 16   [19] 15 \s
                  [2]  6    [8] 12   [14] 17   ...  ...\s
                  [3] 14    [9] 11   [15] 12  [298]  4 \s
                  [4] 11   [10] 23   [16]  9  [299]  6 \s
                  [5] 19   [11] 15   [17] 11 \s
                ]}""", ht.toContent());

        assertEquals("""
                HistogramTable
                ==============
                min=0
                max=1
                bins=300
                freq=[
                  [0]  5  [18] 13  [36] 16  [54] 19  [72] 15  [90] 12 [108] 10 [126] 12 [144] 11 [162]  8 [180] 11 [198] 10 [216] 15 [234] 11 [252] 10 [270]  6 [288]  7\s
                  [1] 15  [19] 15  [37] 20  [55] 18  [73] 12  [91]  7 [109] 20 [127] 20 [145] 13 [163] 13 [181] 13 [199] 10 [217]  3 [235]  8 [253] 14 [271]  8 [289]  8\s
                  [2]  6  [20]  9  [38] 12  [56] 12  [74] 15  [92] 15 [110] 13 [128] 10 [146] 17 [164]  9 [182] 10 [200] 15 [218]  7 [236]  9 [254]  3 [272] 14 [290] 12\s
                  [3] 14  [21] 16  [39]  7  [57]  8  [75] 10  [93] 12 [111] 13 [129] 16 [147] 15 [165]  8 [183] 12 [201]  6 [219]  9 [237]  6 [255]  5 [273]  8 [291]  8\s
                  [4] 11  [22]  8  [40] 11  [58]  8  [76] 10  [94] 19 [112] 16 [130] 14 [148] 12 [166] 12 [184] 17 [202] 12 [220] 11 [238] 13 [256] 10 [274]  7 [292]  9\s
                  [5] 19  [23] 15  [41] 18  [59] 12  [77] 13  [95] 12 [113] 15 [131] 13 [149] 11 [167] 18 [185]  6 [203]  9 [221] 16 [239] 10 [257] 14 [275] 14 [293]  6\s
                  [6]  9  [24] 11  [42] 14  [60] 15  [78] 16  [96] 12 [114] 12 [132] 13 [150] 11 [168] 12 [186] 10 [204] 10 [222]  9 [240] 11 [258] 13 [276]  4 [294]  8\s
                  [7] 14  [25]  9  [43] 18  [61] 11  [79] 11  [97] 14 [115] 14 [133] 16 [151] 11 [169]  9 [187] 12 [205] 13 [223] 11 [241]  9 [259]  7 [277] 10 [295]  4\s
                  [8] 12  [26] 10  [44] 15  [62]  9  [80] 11  [98] 12 [116] 13 [134] 12 [152] 11 [170]  5 [188] 17 [206] 14 [224] 11 [242] 12 [260] 10 [278] 10 [296]  8\s
                  [9] 11  [27] 14  [45] 18  [63] 19  [81] 15  [99] 14 [117] 10 [135] 13 [153] 10 [171]  6 [189] 12 [207] 12 [225] 10 [243]  8 [261]  6 [279]  7 [297]  7\s
                 [10] 23  [28] 13  [46] 12  [64] 13  [82] 13 [100] 12 [118] 11 [136] 12 [154] 18 [172]  8 [190]  9 [208]  3 [226] 15 [244] 10 [262] 14 [280]  8 [298]  4\s
                 [11] 15  [29] 16  [47] 11  [65]  9  [83] 17 [101] 16 [119] 12 [137] 12 [155] 15 [173] 13 [191] 12 [209] 13 [227] 12 [245] 10 [263]  8 [281] 14 [299]  6\s
                 [12] 16  [30] 15  [48] 19  [66]  9  [84]  5 [102]  9 [120] 12 [138] 13 [156] 13 [174] 12 [192] 10 [210] 14 [228]  8 [246]  7 [264] 10 [282]  6\s
                 [13] 16  [31]  9  [49] 16  [67] 15  [85] 10 [103] 10 [121] 17 [139] 10 [157] 12 [175] 13 [193] 10 [211] 11 [229]  7 [247] 14 [265] 12 [283] 10\s
                 [14] 17  [32] 12  [50] 12  [68] 13  [86] 19 [104] 13 [122]  9 [140]  9 [158]  9 [176]  9 [194] 11 [212] 10 [230]  7 [248]  9 [266] 17 [284]  6\s
                 [15] 12  [33]  6  [51]  9  [69] 15  [87] 17 [105] 17 [123] 10 [141] 11 [159] 12 [177] 12 [195]  7 [213] 10 [231] 11 [249] 14 [267]  3 [285]  6\s
                 [16]  9  [34] 16  [52] 12  [70] 11  [88]  9 [106] 16 [124] 15 [142]  8 [160] 13 [178] 13 [196] 12 [214]  9 [232] 12 [250]  9 [268]  8 [286]  7\s
                 [17] 11  [35] 10  [53] 16  [71] 22  [89]  8 [107] 14 [125] 11 [143] 16 [161] 12 [179]  9 [197] 14 [215] 10 [233] 15 [251]  9 [269] 11 [287] 13\s
                ]}""", ht.toFullContent(textWidth(120)));

    }

    @Test
    void testFriedmanDiaconis() {
        Tensor<Double> t = Tensors.random(Shape.of(1_000), random);
        VarDouble v = t.dv();
        HistogramTable ht = new HistogramTable(v, Double.NaN, Double.NaN, 0);
        assertEquals(27, ht.bins());
        assertEquals(t.amin(), ht.min());
        assertEquals(t.amax(), ht.max());
    }

}
