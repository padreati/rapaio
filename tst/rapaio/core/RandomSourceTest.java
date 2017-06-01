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

package rapaio.core;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.IndexVar;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/1/16.
 */
public class RandomSourceTest {
    private RandomSource randomSource = RandomSource.createRandom();


    @Test
    public void reproducibleTest() {

        Var seeds = IndexVar.from(100, i -> i*i);

        randomSource.withUtilRandom(0);
        checkRandom(seeds);
    }

    private void checkRandom(Var seeds) {
        for (int i = 0; i < seeds.getRowCount(); i++) {
            randomSource.setSeed(seeds.getIndex(i));
            int n1 = randomSource.nextInt(1000);
            randomSource.setSeed(seeds.getIndex(i));
            int n2 = randomSource.nextInt(1000);
            Assert.assertEquals(n1, n2);
        }
    }
}
