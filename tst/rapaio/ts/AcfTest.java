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

package rapaio.ts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/29/17.
 */
public class AcfTest {

    private static final double TOL = 1e-15;

    private VarDouble ts1;
    private VarDouble ts2;
    private VarDouble ts3;

    // computed in R using forecast::Acf

    private VarDouble corr2;
    private VarDouble corr3;
    private VarDouble cov2;
    private VarDouble cov3;


    @BeforeEach
    void beforeEach() {

        ts1 = VarDouble.fill(20, 1).name("ts1");
        ts2 = VarDouble.seq(1, 20).name("ts2");
        ts3 = VarDouble.wrap(
                0.6979648509158923, -0.2406058329854418, -1.8975688306241107, 0.6038877149907240, -1.0588252484632350,
                -0.5431671075613055, -0.2601316660744313, 1.5800360521101393, 0.6343785691225632, -1.2059157599825618,
                -0.9344843442712357, -0.9395890575710252, -1.3142520685298062, 0.6052511257189427, 1.3506630966671445,
                -1.4497517922535001, -0.1403198814124193, 1.1962603720850546, -1.1999372448315526, -0.6278085018229833).name("ts3");
        corr2 = VarDouble.wrap(
                0.99999999999999978, 0.84999999999999976, 0.70150375939849607, 0.55601503759398485, 0.41503759398496232,
                0.28007518796992475, 0.15263157894736840, 0.03421052631578946, -0.07368421052631578, -0.16954887218045109,
                -0.25187969924812026, -0.31917293233082700, -0.36992481203007516, -0.40263157894736834, -0.41578947368421043,
                -0.40789473684210520, -0.37744360902255636, -0.32293233082706763, -0.24285714285714277, -0.13571428571428570);

        corr3 = VarDouble.wrap(
                1.00000000000000000, -0.08483871645887758, -0.37360837037828093, 0.08956384227269978, -0.14231675101016045,
                -0.32257643351652959, 0.11176363749001397, 0.26985473385123482, -0.03082066297576692, 0.03983518876574605,
                0.15193314018403464, -0.09179660652770671, -0.29112332693108145, 0.10021816843439253, 0.17114894484083301,
                -0.20094829853780788, 0.06786506328344186, 0.09861045268517940, -0.04513914344640205, -0.01762486202496256
        );

        cov2 = VarDouble.wrap(
                33.250000000000000, 28.262499999999999, 23.324999999999999, 18.487500000000001, 13.800000000000001,
                9.312500000000000, 5.075000000000000, 1.137500000000000, -2.450000000000000, -5.637500000000000,
                -8.375000000000000, -10.612500000000001, -12.300000000000001, -13.387499999999999, -13.824999999999999,
                -13.562500000000000, -12.550000000000001, -10.737500000000001, -8.074999999999999, -4.512500000000000);

        cov3 = VarDouble.wrap(
                1.00424809573654494, -0.08519911944856048, -0.37519549450362238, 0.08994431804920702, -0.14292132619336564,
                -0.32394676908846104, 0.11223842012193605, 0.27100110259559473, -0.03095159210275176, 0.04000441246130628,
                0.15257856670909031, -0.09218656730052636, -0.29236004669502613, 0.10064390480844301, 0.17187600194372551,
                -0.20180194614809230, 0.06815336056943662, 0.09902935932881007, -0.04533089884922800, -0.01769973412618800
        );
    }

    @Test
    void basicTest() {
        Acf acf1 = Acf.from(ts1, ts1.size());
        acf1.printSummary();
        for (int i = 0; i < acf1.correlation().size(); i++) {
            assertTrue(acf1.correlation().isMissing(i));
        }

        Acf acf2 = Acf.from(ts2, ts2.size());
        acf2.printSummary();
        for (int i = 0; i < acf2.correlation().size(); i++) {
            assertEquals(corr2.getDouble(i), acf2.correlation().getDouble(i), TOL);
        }
        for (int i = 0; i < acf2.correlation().size(); i++) {
            assertEquals(cov2.getDouble(i), acf2.covariance().getDouble(i), TOL);
        }

        Acf acf3 = Acf.from(ts3, ts3.size());
        acf3.printSummary();
        for (int i = 0; i < acf3.correlation().size(); i++) {
            assertEquals(cov3.getDouble(i), acf3.covariance().getDouble(i), TOL);
        }
    }
}
