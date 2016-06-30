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

package rapaio.math.fourier;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.sys.WS;
import rapaio.util.Pair;

public class FFTTest {

    // display an array of Complex numbers to standard output
    public static void show(Pair<Var, Var> x) {
        System.out.println("-------------------");
        for (int i = 0; i < x._1.rowCount(); i++) {
            System.out.println(x._1.value(i) + " , " + x._2.value(i));
        }
        System.out.println();
    }

    @Test
    public void baseTest() {

        /***************************************************************************
         * Test client and sample execution
         * <p>
         * % java FFT 4
         * x
         * -------------------
         * -0.03480425839330703
         * 0.07910192950176387
         * 0.7233322451735928
         * 0.1659819820667019
         * <p>
         * y = fft(x)
         * -------------------
         * 0.9336118983487516
         * -0.7581365035668999 + 0.08688005256493803i
         * 0.44344407521182005
         * -0.7581365035668999 - 0.08688005256493803i
         * <p>
         * z = ifft(y)
         * -------------------
         * -0.03480425839330703
         * 0.07910192950176387 + 2.6599344570851287E-18i
         * 0.7233322451735928
         * 0.1659819820667019 - 2.6599344570851287E-18i
         * <p>
         * c = cconvolve(x, x)
         * -------------------
         * 0.5506798633981853
         * 0.23461407150576394 - 4.033186818023279E-18i
         * -0.016542951108772352
         * 0.10288019294318276 + 4.033186818023279E-18i
         * <p>
         * d = convolve(x, x)
         * -------------------
         * 0.001211336402308083 - 3.122502256758253E-17i
         * -0.005506167987577068 - 5.058885073636224E-17i
         * -0.044092969479563274 + 2.1934338938072244E-18i
         * 0.10288019294318276 - 3.6147323062478115E-17i
         * 0.5494685269958772 + 3.122502256758253E-17i
         * 0.240120239493341 + 4.655566391833896E-17i
         * 0.02755001837079092 - 2.1934338938072244E-18i
         * 4.01805098805014E-17i
         ***************************************************************************/

        Numeric xre = Numeric.copy(-0.03480425839330703, 0.07910192950176387, 0.7233322451735928, 0.1659819820667019);
        Numeric xim = Numeric.copy(0, 0, 0, 0);

        int len = 10;

        Pair<Var, Var> x = Pair.from(xre, xim);

        // FFT of original data
        Pair<Var, Var> y = FFT.fft(x);
        Pair<Var, Var> z = FFT.ifft(y);

        WS.println("x");
        show(x);
        WS.println("y=fft(x)");
        show(y);
        WS.println("z=ifft(y)");
        show(z);

        show(FFT.cconvolve(x, x));
        show(FFT.convolve(x, x));
    }

    @Test
    public void randomInverseTest() {

        final int N = 1024;
        RandomSource.setSeed(1234);

        Normal normal = new Normal(0, 100);

        for (int i = 0; i < 10; i++) {

            Numeric x1 = Numeric.from(N, normal::sampleNext);
            Numeric x2 = Numeric.from(N, normal::sampleNext);

            Pair<Var, Var> y = FFT.ifft(FFT.fft(Pair.from(x1, x2)));

            Var y1 = y._1;
            Var y2 = y._2;

            for (int j = 0; j < N; j++) {
                Assert.assertEquals(x1.value(i), y1.value(i), 1e-12);
            }
        }
    }
}
