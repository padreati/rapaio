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

package rapaio.data.matrix;

import org.junit.Test;
import rapaio.data.Numeric;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.QR;
import rapaio.math.linear.dense.SolidRM;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class QRTest {

    @Test
    public void testQR() {
        RM x = SolidRM.copy(new double[][]{
                {1, 1, 1},
                {1, 2, 4},
                {1, 3, 9},
                {1, 4, 16},
                {1, 5, 25}
        });

        RM y = SolidRM.copy(
                Numeric.copy(2.8, 3.2, 7.1, 6.8, 8.8).withName("1"),
                Numeric.copy(2.8, 3.2, 7.1, 6.8, 8.9).withName("2")
        );

        QR qr = new QR(x);
//        System.out.println("Q");
//        qr.getQ().print(new DecimalFormat("0.000000000"), 14);
//        System.out.println("R");
//        qr.getR().print(new DecimalFormat("0.000000000"), 14);
//        System.out.println("QR");
//        qr.getQR().print(new DecimalFormat("0.000000000"), 14);
//        System.out.println("H");
//        qr.getH().print(new DecimalFormat("0.000000000"), 14);

        qr.solve(y).printSummary();

        x.printSummary();
        qr.getQ().dot(qr.getR()).printSummary();
    }
}
