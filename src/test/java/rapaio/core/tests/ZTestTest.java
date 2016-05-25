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

package rapaio.core.tests;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Numeric;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/25/16.
 */
public class ZTestTest {

    @Test
    public void oneSampleTest() {

//        RandomSource.setSeed(1234);
        double mu = 75;
        double sd = 18;
        Var x = Numeric.copy(65, 78, 88, 55, 48, 95, 66, 57, 79, 81);

        Assert.assertEquals("\n" +
                        "> ZTest.oneSample\n" +
                        "\n" +
                        " One Sample z-test\n" +
                        "\n" +
                        "complete rows: 10/10\n" +
                        "mean: 75\n" +
                        "sd: 18\n" +
                        "significance level: 0.05\n" +
                        "alternative hypothesis: two tails P > |z|\n" +
                        "\n" +
                        "sample mean: 71.2\n" +
                        "z score: -0.667592\n" +
                        "p-value: 0.5043940973335608\n" +
                        "conf int: [60.0436894,82.3563106]\n"
                , ZTest.oneSample(x, mu, sd).summary());
        Assert.assertEquals("\n" +
                "> ZTest.oneSample\n" +
                "\n" +
                " One Sample z-test\n" +
                "\n" +
                "complete rows: 10/10\n" +
                "mean: 75\n" +
                "sd: 18\n" +
                "significance level: 0.05\n" +
                "alternative hypothesis: one tail P < z\n" +
                "\n" +
                "sample mean: 71.2\n" +
                "z score: -0.667592\n" +
                "p-value: 0.2521970486667804\n" +
                "conf int: [60.0436894,82.3563106]\n" , ZTest.oneSample(x, mu, sd, 0.05, ZTest.Alternative.LESS_THAN).summary());
        Assert.assertEquals("\n" +
                "> ZTest.oneSample\n" +
                "\n" +
                " One Sample z-test\n" +
                "\n" +
                "complete rows: 10/10\n" +
                "mean: 75\n" +
                "sd: 18\n" +
                "significance level: 0.05\n" +
                "alternative hypothesis: one tail P > z\n" +
                "\n" +
                "sample mean: 71.2\n" +
                "z score: -0.667592\n" +
                "p-value: 0.7478029513332196\n" +
                "conf int: [60.0436894,82.3563106]\n", ZTest.oneSample(x, mu, sd, 0.05, ZTest.Alternative.GREATER_THAN).summary());
    }
}
