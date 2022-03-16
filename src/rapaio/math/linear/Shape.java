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

package rapaio.math.linear;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import rapaio.printer.Printable;
import rapaio.printer.opt.POption;

/**
 * A shape describes the dimensions of a tensor in a generic sense. This includes vectors, arrays and multi-dimensional
 * arrays. A tensor have a number of dimensions greater or equal with one, thus scalars are not modeled through this API,
 * having specific methods which handles this case. Scalars are treated more naturally
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/21.
 */
public record Shape(int[] dims) implements Serializable, Printable {

    @Serial
    private static final long serialVersionUID = 8629690522468837031L;

    public static Shape of(int... lengths) {
        return new Shape(lengths);
    }

    public Shape {
        if (dims == null || dims.length < 1) {
            throw new IllegalArgumentException("Cannot create shape: dimension array is empty or null.");
        }
        for (int x : dims) {
            if (x == 0) {
                throw new IllegalArgumentException("Dimension value cannot be zero.");
            }
        }
    }

    public int size() {
        return dims.length;
    }

    public int get(int axis) {
        return dims[axis];
    }

    @Override
    public String toString() {
        return "[" + Arrays.toString(dims) + "]";
    }

    @Override
    public String toContent(POption<?>... options) {
        return toString();
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toString();
    }

    @Override
    public String toSummary(POption<?>... options) {
        return toString();
    }
}
