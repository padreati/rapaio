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

package rapaio.experiment.sandbox;

import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.data.Index;
import rapaio.data.Mapping;
import rapaio.data.Numeric;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/4/16.
 */
public class ParallelTesting {

    public static void main(String[] args) {

        RandomSource.setSeed(1);

        Numeric x = Numeric.from(10_000, row -> row * 1.0);
        Index index = Index.from(10_000, row -> row);

        index.stream()
                .parallel()
                .map(s -> {
                            try {
                                Thread.sleep(RandomSource.nextInt(500));
                            } catch (InterruptedException ex) {

                            }
                            return (s.index() >= 5_000)
                                    ? CoreTools.mean(x.mapRows(Mapping.range(5_000, s.index() + 1))).value()
                                    : CoreTools.mean(x.mapRows(Mapping.range(0, s.index() + 1))).value();
                        }
                )
                .limit(20)
                .forEach(System.out::println);
    }
}
