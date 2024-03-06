/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.transform;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.correlation.CorrPearson;
import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/3/16.
 */
public class RandomProjectionTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(1);
    }

    @Test
    void gausianSDTest() {

        RandomProjection rp = RandomProjection.newGaussianSd(random, 3, VarRange.all()).newInstance();
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

        RandomProjection rp = RandomProjection.newAchlioptas(random, 3, VarRange.all()).newInstance();
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
        RandomProjection rp = RandomProjection.newAchlioptas(random, 3, 5, VarRange.all()).newInstance();
        Frame df = Datasets.loadIrisDataset().fapply(rp);

        assertEquals(3, df.varCount());
        assertEquals("RP_1", df.rvar(0).name());
        assertEquals("RP_2", df.rvar(1).name());
        assertEquals("RP_3", df.rvar(2).name());

        double corr = CorrPearson.of(df).singleValue();
        assertEquals(-0.5195786390214069, corr, 1e-15);
    }
}
