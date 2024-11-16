/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.nn;

import java.util.List;

import rapaio.util.NotImplementedException;

public interface Net {

    void seed(long seed);

    List<Tensor> parameters();

    void train();

    void eval();

    default Tensor[] forward(Tensor... xs) {
        if (xs.length == 1) {
            return new Tensor[] {forward11(xs[0])};
        }
        throw new NotImplementedException();
    }

    default Tensor forward11(Tensor x) {
        throw new NotImplementedException();
    }
}
