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

package rapaio.ml.clustering;

import org.junit.jupiter.api.Test;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.experiment.ml.clustering.ClusterSilhouette;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class ClusterSilhouetteTest {
    @Test
    public void paperTest() {
        String[] names = new String[]{"BEL", "BRA", "CHI", "CUB", "EGY", "FRA", "IND", "ISR", "USA", "USS", "YUG", "ZAI"};
        DistanceMatrix dm = DistanceMatrix.empty(names);

        double[] values = new double[]{
                5.58,
                7.00, 6.50,
                7.08, 7.00, 3.83,
                4.83, 5.08, 8.17, 5.83,
                2.17, 5.75, 6.67, 6.92, 4.92,
                6.42, 5.00, 5.58, 6.00, 4.67, 6.42,
                3.42, 5.50, 6.42, 6.42, 5.00, 3.92, 6.17,
                2.50, 4.92, 6.25, 7.33, 4.50, 2.25, 6.33, 2.75,
                6.08, 6.67, 4.25, 2.67, 6.00, 6.17, 6.17, 6.92, 6.17,
                5.25, 6.83, 4.50, 3.75, 5.75, 5.42, 6.08, 5.83, 6.67, 3.67,
                4.75, 3.00, 6.08, 6.67, 5.00, 5.58, 4.83, 6.17, 5.67, 6.50, 6.92
        };
        int pos = 0;
        for (int i = 1; i < names.length; i++) {
            for (int j = 0; j < i; j++) {
                dm.set(i, j, values[pos++]);
            }
        }

        Var asgn1 = VarInt.wrap(1, 1, 2, 2, 1, 1, 2, 1, 1, 2, 2, 1);
        Var asgn2 = VarInt.wrap(1, 2, 3, 3, 1, 1, 2, 1, 1, 3, 3, 2);

        ClusterSilhouette cs1 = ClusterSilhouette.from(asgn1, dm, false);
        cs1.printSummary();


        ClusterSilhouette cs2 = ClusterSilhouette.from(asgn2, dm, false);
        cs2.printSummary();

    }

}
