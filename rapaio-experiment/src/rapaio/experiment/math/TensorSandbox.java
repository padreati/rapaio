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

package rapaio.experiment.math;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;

public class TensorSandbox {
    public static void main(String[] args) throws IOException, URISyntaxException {
        var tmd1 = TensorManager.vectorizedArray().ofDouble();
        Random random = new Random(42);
        var t1 = tmd1.random(Shape.of(1_000), random);
        var t2 = tmd1.random(Shape.of(1_000), random);

        t1.printContent();
        t2.printContent();

        t1.max(t2).printContent();
        t1.min(t2).printContent();
    }
}
