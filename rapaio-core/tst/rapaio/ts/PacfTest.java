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

package rapaio.ts;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/17.
 */
public class PacfTest {

    private static final double TOL = 1e-13;

    private VarDouble ts1;
    private VarDouble ts2;
    private VarDouble ts3;

    // computed in R using forecast::Acf

    private VarDouble ref2;
    private VarDouble ref3;


    @BeforeEach
    void beforeEach() {

        ts1 = VarDouble.fill(20, 1).name("ts1");
        ts2 = VarDouble.seq(1, 20).name("ts2");
        ts3 = VarDouble.wrap(
                0.6979648509158923, -0.2406058329854418, -1.8975688306241107, 0.6038877149907240,
                -1.0588252484632350, -0.5431671075613055, -0.2601316660744313, 1.5800360521101393,
                0.6343785691225632, -1.2059157599825618, -0.9344843442712357, -0.9395890575710252,
                -1.3142520685298062, 0.6052511257189427, 1.3506630966671445, -1.4497517922535001,
                -0.1403198814124193, 1.1962603720850546, -1.1999372448315526, -0.6278085018229833
        ).name("ts3");

        ref2 = VarDouble.wrap(
                0.849999999999999756, -0.075662128293706235, -0.076350697060726891, -0.076986286385123531, -0.077470340055607045,
                -0.077678098116149980, -0.077449846796252123, -0.076580332319109073, -0.074806500059323516, -0.071794351849237673,
                -0.067126804527072773, -0.060296031466335422, -0.050705555565239362, -0.037688112631549968, -0.020542168393234521,
                0.001421888810447209, 0.028861216320893255, 0.062486252156896252, 0.103404452912941217);

        ref3 = VarDouble.wrap(
                -0.08483871645887758, -0.38356674116065448, 0.01555188698220464, -0.32324824719487172,
                -0.43207097239004993, -0.31460709753153510, -0.18877219827433805, -0.28427895048627366,
                -0.24893705184301876, -0.17752364799019998, -0.11827176558536588, -0.36049570502238704,
                -0.19733966821910784, -0.03111685847675113, -0.16525578038575647, -0.01996207694747570,
                -0.20301424343853458, 0.01427113035194936, 0.04975825217532487
        );
    }

    @Test
    void basicTest() {

        Pacf pacf2 = Pacf.from(ts2, ts2.size() - 1);
        for (int i = 0; i < pacf2.values().size(); i++) {
            assertEquals(ref2.getDouble(i), pacf2.values().getDouble(i), TOL);
        }

        Pacf pacf3 = Pacf.from(ts3, ts3.size() - 1);
        for (int i = 0; i < pacf3.values().size(); i++) {
            assertEquals(ref3.getDouble(i), pacf3.values().getDouble(i), TOL, "err at i=" + i);
        }
    }
}
