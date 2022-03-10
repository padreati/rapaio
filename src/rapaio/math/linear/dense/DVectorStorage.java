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

package rapaio.math.linear.dense;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;

public interface DVectorStorage {

    int size();

    double[] array();

    DoubleVector loadVector(int i);

    DoubleVector loadVector(int i, VectorMask<Double> m);

    void storeVector(DoubleVector v, int i);

    void storeVector(DoubleVector v, int i, VectorMask<Double> m);

    double get(int i);

    void set(int i, double value);

    void inc(int i, double value);

    ////////////////////////////////

}
