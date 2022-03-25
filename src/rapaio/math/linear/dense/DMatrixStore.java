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
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.linear.DMatrix;

public interface DMatrixStore extends DMatrix {

    VectorSpecies<Double> species();

    int speciesLen();

    DoubleVector loadVectorRow(int row, int i);

    DoubleVector loadVectorRow(int row, int i, VectorMask<Double> m);

    void storeVectorRow(DoubleVector vector, int row, int i);

    void storeVectorRow(DoubleVector vector, int row, int i, VectorMask<Double> m);

    int loopBoundRow();

    VectorMask<Double> loopMaskRow();

    DoubleVector loadVectorCol(int col, int i);

    DoubleVector loadVectorCol(int col, int i, VectorMask<Double> m);

    void storeVectorCol(DoubleVector vector, int col, int i);

    void storeVectorCol(DoubleVector vector, int col, int i, VectorMask<Double> m);

    int loopBoundCol();

    VectorMask<Double> loopMaskCol();

    double[] solidArrayCopy();
}
