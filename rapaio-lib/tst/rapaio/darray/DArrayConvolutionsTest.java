/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.darray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DArrayConvolutionsTest {

    private DArrayManager dm;

    @BeforeEach
    void setUp() {
        dm = DArrayManager.base();
    }

    static Stream<DType<?>> dtSource() {
        return Stream.of(DType.DOUBLE, DType.FLOAT, DType.INTEGER, DType.BYTE);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testSimpleConv1d(DType<?> dt) {
        var p = new ConvParams().inLen(4).k(3);
        double[] exp = new double[] {14, 20};
        double[] texp = new double[] {14, 48, 82, 60};
        testScenarioConv1D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testSimpleGroupsConv1d(DType<?> dt) {
        var p = new ConvParams().n(3).inChannels(8).inLen(4).outChannels(4).k(3).groups(4);
        double[] exp = new double[] {
                106, 127, 706, 763, 1882, 1975,
                3634, 3763, 778, 799, 2530, 2587,
                4858, 4951, 7762, 7891, 1450, 1471,
                4354, 4411, 7834, 7927, 11890, 12019
        };
        double[] texp = new double[] {
                106.0, 339.0, 572.0, 381.0,
                424.0, 1038.0, 1271.0, 762.0,
                4942.0, 10989.0, 12458.0, 6867.0,
                7060.0, 15396.0, 16865.0, 9156.0,
                24466.0, 52023.0, 55880.0, 29625.0,
                30112.0, 63594.0, 67451.0, 35550.0,
                69046.0, 144177.0, 151574.0, 79023.0,
                79948.0, 166368.0, 173765.0, 90312.0,
                778.0, 2355.0, 3932.0, 2397.0,
                3112.0, 7086.0, 8663.0, 4794.0,
                17710.0, 38349.0, 43466.0, 23283.0,
                25300.0, 53700.0, 58817.0, 31044.0,
                63154.0, 132375.0, 142184.0, 74265.0,
                77728.0, 161802.0, 171611.0, 89118.0,
                147478.0, 305169.0, 320822.0, 165711.0,
                170764.0, 352128.0, 367781.0, 189384.0,
                1450.0, 4371.0, 7292.0, 4413.0,
                5800.0, 13134.0, 16055.0, 8826.0,
                30478.0, 65709.0, 74474.0, 39699.0,
                43540.0, 92004.0, 100769.0, 52932.0,
                101842.0, 212727.0, 228488.0, 118905.0,
                125344.0, 260010.0, 275771.0, 142686.0,
                225910.0, 466161.0, 490070.0, 252399.0,
                261580.0, 537888.0, 561797.0, 288456.0,
        };
        testScenarioConv1D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testStrideConv1d(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inLen(8).outChannels(4).k(3).stride(2).outputPadding(1);
        double[] exp = new double[] {
                166, 208, 250,
                382, 496, 610,
                598, 784, 970,
                814, 1072, 1330};
        double[] texp = new double[] {
                26080.0, 28040.0, 64240.0, 36800.0, 81760.0, 45560.0, 48720.0, 0.0,
                31960.0, 33920.0, 77800.0, 44480.0, 98920.0, 55040.0, 58200.0, 0.0
        };
        testScenarioConv1D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testDilationConv1d(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inLen(8).outChannels(4).k(3).dilation(2);
        double[] exp = new double[] {
                191, 212, 233, 254,
                443, 500, 557, 614,
                695, 788, 881, 974,
                947, 1076, 1205, 1334};
        double[] texp = new double[] {
                30320., 34400., 71076., 79536., 76228., 85288., 44232., 48912.,
                37148., 42128., 86532., 96792., 91684., 102544., 52860., 58440.
        };
        testScenarioConv1D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testPaddingConv1d(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inLen(8).outChannels(4).k(3).padding(2);
        double[] exp = new double[] {
                57, 113, 166, 187, 208, 229, 250, 271, 163, 72,
                117, 245, 382, 439, 496, 553, 610, 667, 439, 216,
                177, 377, 598, 691, 784, 877, 970, 1063, 715, 360,
                237, 509, 814, 943, 1072, 1201, 1330, 1459, 991, 504};
        double[] texp = new double[] {
                52580.0, 77088.0, 96660.0, 109800.0, 122940.0, 136080.0, 130020.0, 102908.0,
                63956.0, 93480.0, 117000.0, 132840.0, 148680.0, 164520.0, 156804.0, 123668.0
        };
        testScenarioConv1D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testPaddingStrideConv1d(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inLen(8).outChannels(4).k(3).padding(2).stride(2).outputPadding(1);
        double[] exp = new double[] {
                57, 166, 208, 250, 163,
                117, 382, 496, 610, 439,
                177, 598, 784, 970, 715,
                237, 814, 1072, 1330, 991};
        double[] texp = new double[] {
                34936.0, 28040.0, 64240.0, 36800.0, 81760.0, 45560.0, 80080.0, 33668.0,
                42580.0, 33920.0, 77800.0, 44480.0, 98920.0, 55040.0, 96484.0, 40592.0
        };
        testScenarioConv1D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testPaddingStrideDilation(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inLen(8).outChannels(4).k(3).padding(3).stride(2).dilation(3).outputPadding(1);
        double[] exp1d = new double[] {
                131, 163, 237, 141,
                287, 367, 561, 369,
                443, 571, 885, 597,
                599, 775, 1209, 825};
        double[] expt1d = new double[] {
                20740., 38640., 26756., 48360., 41532., 28632., 28092., 44424.,
                25120., 47316., 32384., 58536., 50208., 34260., 33888., 53100.
        };
        testScenarioConv1D(dt, p, exp1d, expt1d);
    }

    void testScenarioConv1D(DType<?> dt, ConvParams p, double[] exp, double[] texp) {

        var in = dm.seq(dt, Shape.of(p.n, p.inChannels, p.inLen)).add(1);
        var kernel = dm.seq(dt, Shape.of(p.outChannels, Math.floorDiv(p.inChannels, p.groups), p.k)).add(1);
        var y = in.conv1d(kernel, null, p.stride, p.padding, p.dilation, p.groups);

        int outLen = Math.floorDiv(p.inLen + 2 * p.padding - (p.k - 1) * p.dilation, p.stride);
        var expected = dm.stride(dt, exp).reshape(Shape.of(p.n, p.outChannels, outLen));

        assertEquals(Shape.of(p.n, p.outChannels, outLen), y.shape());
        assertTrue(expected.deepEquals(y));

        var out = y.convTranspose1d(kernel, null, p.stride, p.padding, p.dilation, p.groups, p.outputPadding);
        var texpected = dm.stride(dt, texp).reshape(in.shape());

        assertEquals(texpected.shape(), out.shape());
        assertTrue(texpected.deepEquals(out));
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testSimpleConv2d(DType<?> dt) {
        var p = new ConvParams().inH(3).inW(3).kH(2).kW(2);
        double[] exp = {37, 47, 67, 77};
        double[] texp = {37, 121, 94, 178, 500, 342, 201, 499, 308};
        testScenarioConv2D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testMultiChannelConv2d(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inH(3).inW(3).outChannels(2).kH(2).kW(2);
        double[] exp = {356, 392, 464, 500, 836, 936, 1136, 1236};
        double[] texp = {
                7880, 17888, 10144, 20952, 46840, 26160, 13888, 30584, 16832,
                12648, 27968, 15456, 32120, 70264, 38416, 20288, 43928, 23776
        };
        testScenarioConv2D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testPaddingConv2d(DType<?> dt) {
        var p = new ConvParams().inH(3).inW(3).kH(3).kW(3).padding(1);
        double[] exp = {94, 154, 106, 186, 285, 186, 106, 154, 94};
        double[] texp = {1743, 3072, 2681, 4266, 6825, 5524, 4629, 7038, 5447};
        testScenarioConv2D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testStrideConv2d(DType<?> dt) {
        var p = new ConvParams().inH(4).inW(4).kH(2).kW(2).stride(2);
        double[] exp = {44, 64, 124, 144};
        double[] texp = {44, 88, 64, 128, 132, 176, 192, 256, 124, 248, 144, 288, 372, 496, 432, 576};
        testScenarioConv2D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testDilationConv2d(DType<?> dt) {
        var p = new ConvParams().inH(4).inW(4).kH(2).kW(2).dilation(2);
        double[] exp = {78, 88, 118, 128};
        double[] texp = {78, 88, 156, 176, 118, 128, 236, 256, 234, 264, 312, 352, 354, 384, 472, 512};
        testScenarioConv2D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testGroupsConv2d(DType<?> dt) {
        var p = new ConvParams().inChannels(4).inH(3).inW(3).outChannels(4).kH(2).kW(2).groups(2);
        double[] exp = {356, 392, 464, 500, 836, 936, 1136, 1236, 4268, 4432, 4760, 4924, 5900, 6128, 6584, 6812};
        double[] texp = {
                7880, 17888, 10144, 20952, 46840, 26160, 13888, 30584, 16832,
                12648, 27968, 15456, 32120, 70264, 38416, 20288, 43928, 23776,
                220056, 458768, 239104, 485912, 1011096, 525968, 268208, 557032, 289216,
                260728, 541680, 281344, 571960, 1186328, 615152, 313584, 649352, 336160
        };
        testScenarioConv2D(dt, p, exp, texp);
    }

    void testScenarioConv2D(DType<?> dt, ConvParams p, double[] exp, double[] texp) {
        var in = dm.seq(dt, Shape.of(p.n, p.inChannels, p.inH, p.inW)).add(1);
        var kernel = dm.seq(dt, Shape.of(p.outChannels, p.inChannels / p.groups, p.kH, p.kW)).add(1);
        var y = in.conv2d(kernel, null, p.stride, p.padding, p.dilation, p.groups);

        int outH = Math.floorDiv(p.inH + 2 * p.padding - p.dilation * (p.kH - 1) - 1, p.stride) + 1;
        int outW = Math.floorDiv(p.inW + 2 * p.padding - p.dilation * (p.kW - 1) - 1, p.stride) + 1;
        var expected = dm.stride(dt, exp).reshape(Shape.of(p.n, p.outChannels, outH, outW));
        assertEquals(expected.shape(), y.shape());
        assertTrue(expected.deepEquals(y));

        var out = y.convTranspose2d(kernel, null, p.stride, p.padding, p.dilation, p.groups, p.outputPadding);
        var texpected = dm.stride(dt, texp).reshape(in.shape());
        assertEquals(texpected.shape(), out.shape());
        assertTrue(texpected.deepEquals(out));
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testSimpleConv3d(DType<?> dt) {
        var p = new ConvParams().inD(3).inH(3).inW(3).kD(2).kH(2).kW(2);
        double[] exp = {356, 392, 464, 500,
                680, 716, 788, 824};
        double[] texp = {
                356, 1104, 784, 1532, 4028, 2568, 1392, 3356,
                2000, 2460, 6172, 3784, 7640, 18144, 10648, 5612, 12836, 7296,
                3400, 7660, 4296, 8700, 19300, 10672, 5516, 12072, 6592};
        testScenarioConv3D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testMultiChannelConv3d(DType<?> dt) {
        var p = new ConvParams().inChannels(2).inD(3).inH(3).inW(3).outChannels(2).kD(2).kH(2).kW(2);
        double[] exp = {
                3892, 4028, 4300, 4436, 5116, 5252, 5524, 5660,
                9268, 9660, 10444, 10836, 12796, 13188, 13972, 14364};
        double[] texp = {
                161448, 342856, 181936, 369616, 781792, 413232, 211336, 445272, 234464,
                436736, 920256, 484576, 982752, 2064352, 1083712, 552352, 1156768, 605472,
                294296, 615416, 321648, 651152, 1358592, 708496, 360024, 749512,
                390016, 266728, 557640, 291440, 592848, 1236704, 644912, 329288,
                685400, 356640, 685312, 1425856, 741600, 1505248, 3126240, 1623104,
                826272, 1713056, 887840, 437592, 906232, 469168, 950416, 1965568,
                1016208, 515992, 1065672, 550208};
        testScenarioConv3D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testPaddingConv3d(DType<?> dt) {
        var p = new ConvParams().inD(3).inH(3).inW(3).kD(3).kH(3).kW(3).padding(1);
        double[] exp = {
                1412, 2198, 1508, 2370, 3648, 2478, 1652, 2522, 1700, 2982, 4512, 3018,
                4608, 6930, 4608, 3018, 4512, 2982, 1700, 2522, 1652, 2478, 3648, 2370, 1508, 2198, 1412};
        double[] texp = {
                159996, 249720, 197284, 299916, 456162, 352448, 273084, 408276, 310636, 445128, 658686,
                496460, 726642, 1066044, 797062, 598116, 871290, 647096, 512004, 740508, 546196, 784800,
                1130262, 830276, 611916, 877896, 642484};
        testScenarioConv3D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testStrideConv3d(DType<?> dt) {
        var p = new ConvParams().inD(4).inH(4).inW(4).kD(2).kH(2).kW(2).stride(2);
        double[] exp = {560, 632, 848, 920, 1712, 1784, 2000, 2072};
        double[] texp = {
                560, 1120, 632, 1264, 1680, 2240, 1896, 2528, 848, 1696, 920, 1840, 2544, 3392, 2760, 3680,
                2800, 3360, 3160, 3792, 3920, 4480, 4424, 5056, 4240, 5088, 4600, 5520, 5936, 6784, 6440,
                7360, 1712, 3424, 1784, 3568, 5136, 6848, 5352, 7136, 2000, 4000, 2072, 4144, 6000, 8000,
                6216, 8288, 8560, 10272, 8920, 10704, 11984, 13696, 12488, 14272, 10000, 12000, 10360, 12432,
                14000, 16000, 14504, 16576};
        testScenarioConv3D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testDilationConv3d(DType<?> dt) {
        var p = new ConvParams().inD(4).inH(4).inW(4).kD(2).kH(2).kW(2).dilation(2);
        double[] exp = {1084, 1120, 1228, 1264, 1660, 1696, 1804, 1840};
        double[] texp = {
                1084, 1120, 2168, 2240, 1228, 1264, 2456, 2528, 3252, 3360, 4336, 4480, 3684, 3792, 4912, 5056, 1660,
                1696, 3320, 3392, 1804, 1840, 3608, 3680, 4980, 5088, 6640, 6784, 5412, 5520, 7216, 7360, 5420, 5600,
                6504, 6720, 6140, 6320, 7368, 7584, 7588, 7840, 8672, 8960, 8596, 8848, 9824, 10112, 8300, 8480, 9960,
                10176, 9020, 9200, 10824, 11040, 11620, 11872, 13280, 13568, 12628, 12880, 14432, 14720};
        testScenarioConv3D(dt, p, exp, texp);
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testGroupsConv3d(DType<?> dt) {
        var p = new ConvParams().inChannels(4).inD(3).inH(3).inW(3).outChannels(4).kD(2).kH(2).kW(2).groups(2);
        double[] exp = {
                3892, 4028, 4300, 4436, 5116, 5252, 5524, 5660, 9268, 9660, 10444, 10836, 12796, 13188, 13972, 14364, 49636, 50284, 51580,
                52228, 55468, 56116, 57412, 58060, 68836, 69740, 71548, 72452, 76972, 77876, 79684, 80588};
        double[] texp = {
                161448, 342856, 181936, 369616, 781792, 413232, 211336, 445272, 234464, 436736, 920256, 484576, 982752, 2064352, 1083712,
                552352, 1156768, 605472, 294296, 615416, 321648, 651152, 1358592, 708496, 360024, 749512, 390016, 266728, 557640, 291440,
                592848, 1236704, 644912, 329288, 685400, 356640, 685312, 1425856, 741600, 1505248, 3126240, 1623104, 826272, 1713056,
                887840,
                437592, 906232, 469168, 950416, 1965568, 1016208, 515992, 1065672, 550208, 5010952, 10206056, 5196656, 10455888, 21287840,
                10835056, 5454248, 11100408, 5647712, 11086912, 22562304, 11478496, 23088352, 46969184, 23887040, 12020064, 24444128,
                12427168,
                6131832, 12467992, 6337712, 12744208, 25904832, 13163728, 6621688, 13455464, 6835328, 5958728, 12114024, 6156848, 12388688,
                25178272, 12792688, 6439272, 13082872, 6645152, 13094208, 26601728, 13510624, 27177440, 55197024, 28025792, 14101856,
                28632544,
                14533792, 7191352, 14599448, 7409648, 14900496, 30242240, 15344848, 7718456, 15661416, 7944512};
        testScenarioConv3D(dt, p, exp, texp);
    }

    void testScenarioConv3D(DType<?> dt, ConvParams p, double[] exp, double[] texp) {
        var in = dm.seq(dt, Shape.of(p.n, p.inChannels, p.inD, p.inH, p.inW)).add(1);
        var kernel = dm.seq(dt, Shape.of(p.outChannels, p.inChannels / p.groups, p.kD, p.kH, p.kW)).add(1);
        var y = in.conv3d(kernel, null, p.stride, p.padding, p.dilation, p.groups);

        int outD = Math.floorDiv(p.inD + 2 * p.padding - p.dilation * (p.kD - 1) - 1, p.stride) + 1;
        int outH = Math.floorDiv(p.inH + 2 * p.padding - p.dilation * (p.kH - 1) - 1, p.stride) + 1;
        int outW = Math.floorDiv(p.inW + 2 * p.padding - p.dilation * (p.kW - 1) - 1, p.stride) + 1;
        var expected = dm.stride(dt, exp).reshape(Shape.of(p.n, p.outChannels, outD, outH, outW));
        assertEquals(expected.shape(), y.shape());
        assertTrue(expected.deepEquals(y));

        var out = y.convTranspose3d(kernel, null, p.stride, p.padding, p.dilation, p.groups, p.outputPadding);
        var texpected = dm.stride(dt, texp).reshape(in.shape());
        assertEquals(texpected.shape(), out.shape());
        assertTrue(texpected.deepEquals(out));
    }

    static class ConvParams {
        public int n = 1;
        public int inChannels = 1;
        public int inLen = -1;
        public int inD = -1;
        public int inH = -1;
        public int inW = -1;
        public int outChannels = 1;
        public int k = -1;
        public int kD = -1;
        public int kH = -1;
        public int kW = -1;
        public int groups = 1;
        public int padding = 0;
        public int stride = 1;
        public int dilation = 1;
        public int outputPadding = 0;

        ConvParams n(int n) {
            this.n = n;
            return this;
        }

        ConvParams inChannels(int inChannels) {
            this.inChannels = inChannels;
            return this;
        }

        ConvParams inLen(int inLen) {
            this.inLen = inLen;
            return this;
        }

        ConvParams inD(int inD) {
            this.inD = inD;
            return this;
        }

        ConvParams inH(int inH) {
            this.inH = inH;
            return this;
        }

        ConvParams inW(int inW) {
            this.inW = inW;
            return this;
        }

        ConvParams outChannels(int outChannels) {
            this.outChannels = outChannels;
            return this;
        }

        ConvParams k(int k) {
            this.k = k;
            return this;
        }

        ConvParams kD(int kD) {
            this.kD = kD;
            return this;
        }

        ConvParams kH(int kH) {
            this.kH = kH;
            return this;
        }

        ConvParams kW(int kW) {
            this.kW = kW;
            return this;
        }

        ConvParams groups(int groups) {
            this.groups = groups;
            return this;
        }

        ConvParams padding(int padding) {
            this.padding = padding;
            return this;
        }

        ConvParams stride(int stride) {
            this.stride = stride;
            return this;
        }

        ConvParams dilation(int dilation) {
            this.dilation = dilation;
            return this;
        }

        ConvParams outputPadding(int outputPadding) {
            this.outputPadding = outputPadding;
            return this;
        }
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testMaxPool1d(DType<?> dt) {

        var input = dm.seq(dt, Shape.of(1, 9));

        var r1 = input.maxPool1d(2, 2, 0, 1, true);
        testMaxPool1dMethod(dt, input, r1.v1, r1.v2,
                new int[] {1, 3, 5, 7, 8},
                new int[] {1, 3, 5, 7, 8},
                Shape.of(1, 5));

        var r2 = input.maxPool1d(2, 2, 0, 1, false);
        testMaxPool1dMethod(dt, input, r2.v1, r2.v2,
                new int[] {1, 3, 5, 7},
                new int[] {1, 3, 5, 7},
                Shape.of(1, 4));

        var r3 = input.maxPool1d(3, 1, 1, 1, true);
        testMaxPool1dMethod(dt, input, r3.v1, r3.v2,
                new int[] {1, 2, 3, 4, 5, 6, 7, 8, 8},
                new int[] {1, 2, 3, 4, 5, 6, 7, 8, 8},
                Shape.of(1, 9));

        var r4 = input.maxPool1d(2, 1, 0, 2, false);
        testMaxPool1dMethod(dt, input, r4.v1, r4.v2,
                new int[] {2, 3, 4, 5, 6, 7, 8},
                new int[] {2, 3, 4, 5, 6, 7, 8},
                Shape.of(1, 7));

        input = dm.seq(dt, Shape.of(3, 2, 5));

        var r5 = input.maxPool1d(2, 2, 0, 1, false);
        testMaxPool1dMethod(dt, input, r5.v1, r5.v2,
                new int[] {1, 3, 6, 8, 11, 13, 16, 18, 21, 23, 26, 28},
                new int[] {1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3},
                Shape.of(3, 2, 2));
    }

    void testMaxPool1dMethod(DType<?> dt, DArray<?> input,
            DArray<?> poolValue, DArray<Integer> poolIndex,
            int[] expectedValues, int[] expectedIndices,
            Shape expectedShape) {

        var expValues = dm.stride(dt, expectedValues).reshape(expectedShape);
        var expIndices = dm.stride(DType.INTEGER, expectedIndices).reshape(expectedShape);

        assertTrue(expValues.deepEquals(poolValue));
        assertTrue(expIndices.deepEquals(poolIndex));

    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testMaxPool2d(DType<?> dt) {
        // Basic 2x2 pooling with stride 2
        var input = dm.seq(dt, Shape.of(1, 1, 4, 4));
        var r1 = input.maxPool2d(2, 2, 2, 0, 1, false);
        testMaxPool2dMethod(dt, r1.v1, r1.v2,
                new int[] {5, 7, 13, 15},
                new int[] {5, 7, 13, 15},
                Shape.of(1, 1, 2, 2));

        // 3x3 pooling with stride 1, padding 1
        var r2 = input.maxPool2d(3, 3, 1, 1, 1, false);
        testMaxPool2dMethod(dt, r2.v1, r2.v2,
                new int[] {5, 6, 7, 7, 9, 10, 11, 11, 13, 14, 15, 15, 13, 14, 15, 15},
                new int[] {5, 6, 7, 7, 9, 10, 11, 11, 13, 14, 15, 15, 13, 14, 15, 15},
                Shape.of(1, 1, 4, 4));

        // 2x2 pooling with dilation 2
        var r3 = input.maxPool2d(2, 2, 1, 0, 2, false);
        testMaxPool2dMethod(dt, r3.v1, r3.v2,
                new int[] {10, 11, 14, 15},
                new int[] {10, 11, 14, 15},
                Shape.of(1, 1, 2, 2));

        // Multi-channel input
        input = dm.seq(dt, Shape.of(2, 3, 4, 4));
        var r4 = input.maxPool2d(2, 2, 2, 0, 1, false);
        testMaxPool2dMethod(dt, r4.v1, r4.v2,
                new int[] {5, 7, 13, 15, 21, 23, 29, 31, 37, 39, 45, 47,
                        53, 55, 61, 63, 69, 71, 77, 79, 85, 87, 93, 95},
                new int[] {5, 7, 13, 15, 5, 7, 13, 15, 5, 7, 13, 15,
                        5, 7, 13, 15, 5, 7, 13, 15, 5, 7, 13, 15},
                Shape.of(2, 3, 2, 2));

        // ceil_mode=true
        input = dm.seq(dt, Shape.of(1, 1, 5, 5));
        var r5 = input.maxPool2d(2, 2, 2, 0, 1, true);
        testMaxPool2dMethod(dt, r5.v1, r5.v2,
                new int[] {6, 8, 9, 16, 18, 19, 21, 23, 24},
                new int[] {6, 8, 9, 16, 18, 19, 21, 23, 24},
                Shape.of(1, 1, 3, 3));
    }

    void testMaxPool2dMethod(DType<?> dt, DArray<?> poolValue, DArray<Integer> poolIndex,
            int[] expectedValues, int[] expectedIndices, Shape expectedShape) {
        var expValues = dm.stride(dt, expectedValues).reshape(expectedShape);
        var expIndices = dm.stride(DType.INTEGER, expectedIndices).reshape(expectedShape);
        assertTrue(expValues.deepEquals(poolValue));
        assertTrue(expIndices.deepEquals(poolIndex));
    }

    @ParameterizedTest
    @MethodSource("dtSource")
    void testMaxPool3d(DType<?> dt) {
        // Basic 2x2x2 pooling with stride 1
        var input = dm.seq(dt, Shape.of(1, 1, 3, 3, 3));
        var r1 = input.maxPool3d(2, 2, 2, 1, 0, 1, false);
        testMaxPool3dMethod(dt, r1.v1, r1.v2,
                new int[] {13, 14, 16, 17, 22, 23, 25, 26},
                new int[] {13, 14, 16, 17, 22, 23, 25, 26},
                Shape.of(1, 1, 2, 2, 2));

        // 2x2x2 pooling with stride 2
        input = dm.seq(dt, Shape.of(1, 1, 4, 4, 4));
        var r2 = input.maxPool3d(2, 2, 2, 2, 0, 1, false);
        testMaxPool3dMethod(dt, r2.v1, r2.v2,
                new int[] {21, 23, 29, 31, 53, 55, 61, 63},
                new int[] {21, 23, 29, 31, 53, 55, 61, 63},
                Shape.of(1, 1, 2, 2, 2));

        // 2x2x2 pooling with padding 1
        input = dm.seq(dt, Shape.of(1, 1, 3, 3, 3));
        var r3 = input.maxPool3d(2, 2, 2, 2, 1, 1, false);
        testMaxPool3dMethod(dt, r3.v1, r3.v2,
                new int[] {0, 2, 6, 8, 18, 20, 24, 26},
                new int[] {0, 2, 6, 8, 18, 20, 24, 26},
                Shape.of(1, 1, 2, 2, 2));

        // Multi-channel input
        input = dm.seq(dt, Shape.of(2, 2, 4, 4, 4));
        var r4 = input.maxPool3d(2, 2, 2, 2, 0, 1, false);
        testMaxPool3dMethod(dt, r4.v1, r4.v2,
                new int[] {21, 23, 29, 31, 53, 55, 61, 63, 85, 87, 93, 95, 117, 119, 125, 127,
                        149, 151, 157, 159, 181, 183, 189, 191, 213, 215, 221, 223, 245, 247, 253, 255},
                new int[] {21, 23, 29, 31, 53, 55, 61, 63, 21, 23, 29, 31, 53, 55, 61, 63,
                        21, 23, 29, 31, 53, 55, 61, 63, 21, 23, 29, 31, 53, 55, 61, 63},
                Shape.of(2, 2, 2, 2, 2));

        // ceil_mode=true
        input = dm.seq(dt, Shape.of(1, 1, 5, 5, 5));
        var r5 = input.maxPool3d(2, 2, 2, 2, 0, 1, true);
        testMaxPool3dMethod(dt, r5.v1, r5.v2,
                new int[] {31, 33, 34, 41, 43, 44, 46, 48, 49,
                        81, 83, 84, 91, 93, 94, 96, 98, 99,
                        106, 108, 109, 116, 118, 119, 121, 123, 124},
                new int[] {31, 33, 34, 41, 43, 44, 46, 48, 49,
                        81, 83, 84, 91, 93, 94, 96, 98, 99,
                        106, 108, 109, 116, 118, 119, 121, 123, 124},
                Shape.of(1, 1, 3, 3, 3));
    }

    void testMaxPool3dMethod(DType<?> dt, DArray<?> poolValue, DArray<Integer> poolIndex,
            int[] expectedValues, int[] expectedIndices, Shape expectedShape) {
        var expValues = dm.stride(dt, expectedValues).reshape(expectedShape);
        var expIndices = dm.stride(DType.INTEGER, expectedIndices).reshape(expectedShape);
        assertTrue(expValues.deepEquals(poolValue));
        assertTrue(expIndices.deepEquals(poolIndex));
    }
}
