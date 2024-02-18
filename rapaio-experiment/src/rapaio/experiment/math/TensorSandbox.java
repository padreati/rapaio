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
import rapaio.math.tensor.Tensors;

public class TensorSandbox {
    public static void main(String[] args) throws IOException, URISyntaxException {

        int n = 10;
        int m = 100;

        var random = new Random(42);
        var t = Tensors.random(Shape.of(n, m), random);
        System.out.println(t);


        System.out.println(t.t().scatter());

        var mean = t.mean(1).reshape(Shape.of(10, 1));
        var r = Tensors.zeros(Shape.of(n, n));

        for (int i = 0; i < t.dim(1); i++) {
            r.add_(t.take(1, i).sub(mean).mm(t.take(1, i).sub(mean).t()));
        }
        System.out.println(r);

    }
}
