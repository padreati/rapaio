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

package rapaio.core.tools;

import java.io.Serial;
import java.io.Serializable;
import java.util.stream.IntStream;

import rapaio.math.linear.DMatrix;
import rapaio.util.function.IntInt2DoubleBiFunction;

/**
 * Holds a matrix with distances between instances for a given metric.
 * It can be used to compute the distance matrix itself or just to store
 * it for later use.
 * <p>
 * The distance matrix is squared and symmetric, so for storing purposes
 * we hold only the upper triangular shape. Any update on an element
 * updates also the symmetric one, since it is stored only ne.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class DistanceMatrix implements Serializable {

    @Serial
    private static final long serialVersionUID = 1663354103398810554L;

    public static DistanceMatrix empty(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be positive.");
        }
        String[] names = IntStream.range(0, length).mapToObj(String::valueOf).toArray(String[]::new);
        return new DistanceMatrix(names);
    }

    public static DistanceMatrix empty(String[] names) {
        if (names == null || names.length == 0) {
            throw new IllegalArgumentException("Length must be positive.");
        }
        return new DistanceMatrix(names);
    }

    private final String[] names;
    private final double[] values;

    private DistanceMatrix(String[] names) {
        this.names = names;
        int len = names.length;
        values = new double[(len + 1) * len / 2];
    }

    public int length() {
        return names.length;
    }

    public String[] names() {
        return names;
    }

    public String name(int i) {
        return names[i];
    }

    public void set(int i, int j, double value) {
        if (j > i) {
            set(j, i, value);
            return;
        }
        int pos = i * (i + 1) / 2 + j;
        values[pos] = value;
    }

    public double get(int i, int j) {
        if (j > i) {
            return get(j, i);
        }
        int pos = i * (i + 1) / 2 + j;
        return values[pos];
    }

    public DistanceMatrix fill(IntInt2DoubleBiFunction fillFunction) {
        for (int i = 0; i < length(); i++) {
            for (int j = i; j < length(); j++) {
                double value = fillFunction.applyIntIntAsDouble(i, j);
                set(i, j, value);
            }
        }
        return this;
    }

    public DMatrix toDMatrix() {
        return DMatrix.fill(length(), length(), this::get);
    }
}
