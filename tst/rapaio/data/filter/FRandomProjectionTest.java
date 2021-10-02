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

package rapaio.data.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.correlation.CorrPearson;
import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/3/16.
 */
public class FRandomProjectionTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1);
    }

    @Test
    void gausianSDTest() {

        FRandomProjection rp = FRandomProjection.newGaussianSd(3, VarRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fapply(rp);

        assertEquals(3, df.varCount());
        assertEquals("RP_1", df.rvar(0).name());
        assertEquals("RP_2", df.rvar(1).name());
        assertEquals("RP_3", df.rvar(2).name());

        double corr = CorrPearson.of(df).singleValue();
        assertEquals(0.4085654587641364, corr, 1e-12);
    }

    @Test
    void achioptasTest() {

        FRandomProjection rp = FRandomProjection.newAchlioptas(3, VarRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fapply(rp);

        assertEquals(3, df.varCount());
        assertEquals("RP_1", df.rvar(0).name());
        assertEquals("RP_2", df.rvar(1).name());
        assertEquals("RP_3", df.rvar(2).name());


        double corr = CorrPearson.of(df).singleValue();
        assertEquals(-0.5035565970961098, corr, 1e-12);
    }

    @Test
    void achioptas5Test() {

        RandomSource.setSeed(1);
        FRandomProjection rp = FRandomProjection.newAchlioptas(3, 5, VarRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fapply(rp);

        assertEquals(3, df.varCount());
        assertEquals("RP_1", df.rvar(0).name());
        assertEquals("RP_2", df.rvar(1).name());
        assertEquals("RP_3", df.rvar(2).name());

        double corr = CorrPearson.of(df).singleValue();
        assertEquals(-0.5195786390214069, corr, 1e-15);
    }
}
