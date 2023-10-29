/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import rapaio.printer.Format;
import rapaio.printer.opt.POpts;
import rapaio.sys.WS;

public class SandboxEngineTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

//    @Test
    void sandbox() {
        WS.getPrinter().withOptions(POpts.floatFormat(Format.floatShort()));

        var fact = TensorEngines.newDefault();
        var t1 = fact.ofFloat().random(Shape.of(5, 5, 2), random);
        t1.printContent();

        for(var t : t1.slice(1, 3)) {
            t.squeeze().printContent();
        }

        fact.ofFloat().concatenate(1, t1.slice(1, 1).toArray(FTensor[]::new)).printContent();
        fact.ofFloat().stack(1, t1.slice(1, 1).toArray(FTensor[]::new)).squeeze().printContent();

        Assertions.assertTrue(t1.deepEquals(fact.ofFloat().concatenate(1, t1.slice(1, 3).toArray(FTensor[]::new))));

    }
}
