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
        var y = in.conv1d(kernel, null, p.padding, p.stride, p.dilation, p.groups);
        printPython("out", y);

        int outLen = Math.floorDiv(p.inLen + 2 * p.padding - (p.k - 1) * p.dilation, p.stride);
        var expected = dm.stride(dt, exp).reshape(Shape.of(p.n, p.outChannels, outLen));
        printPython("exp", expected);

        assertEquals(Shape.of(p.n, p.outChannels, outLen), y.shape());
        assertTrue(expected.deepEquals(y));

        var out = y.convTranspose1d(kernel, null, p.padding, p.stride, p.dilation, p.groups, p.outputPadding);
        printPython("tout", out);

        var texpected = dm.stride(dt, texp).reshape(in.shape());
        printPython("texp", texpected);

        assertEquals(texpected.shape(), out.shape());
        assertTrue(texpected.deepEquals(out));
    }


    private void printPython(String name, DArray<?> array) {
        System.out.println(name + ":");
        System.out.print("[");
        for (int i = 0; i < array.dim(0); i++) {
            System.out.print("[");
            for (int j = 0; j < array.dim(1); j++) {
                System.out.print("[");
                for (int k = 0; k < array.dim(2); k++) {
                    System.out.print(array.get(i, j, k));
                    if (k < array.dim(2) - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println("],");
            }
            System.out.print("],");
        }
        System.out.println("]");
    }

    static class ConvParams {
        public int n = 1;
        public int inChannels = 1;
        public int inLen = -1;
        public int outChannels = 1;
        public int k = -1;
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

        ConvParams outChannels(int outChannels) {
            this.outChannels = outChannels;
            return this;
        }

        ConvParams k(int k) {
            this.k = k;
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
}
