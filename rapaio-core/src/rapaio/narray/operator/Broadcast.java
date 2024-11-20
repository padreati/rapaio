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

package rapaio.narray.operator;

import java.util.List;

import rapaio.narray.NArray;
import rapaio.narray.Shape;
import rapaio.util.collection.IntArrays;

public final class Broadcast {

    public static ElementWise elementWise(Shape... shapes) {
        return elementWise(List.of(shapes));
    }

    public static ElementWise elementWise(List<Shape> shapes) {
        if (shapes.isEmpty()) {
            return new ElementWise(true, true);
        }
        int len = 0;
        int[] ranks = new int[shapes.size()];
        for (int i = 0; i < shapes.size(); i++) {
            int rank = shapes.get(i).rank();
            ranks[i] = rank;
            len = Math.max(rank, len);
        }
        int[] dims = IntArrays.newFill(len, 1);
        boolean unchanged = true;
        for (int i = 1; i <= len; i++) {
            int max = ranks[0] - i < 0 ? 0 : shapes.getFirst().dim(ranks[0] - i);
            for (int j = 1; j < shapes.size(); j++) {
                max = Math.max(max, ranks[j] - i < 0 ? 0 : shapes.get(j).dim(ranks[j] - i));
            }
            dims[len - i] = max;
            for (int j = 0; j < shapes.size(); j++) {
                int index = ranks[j] - i;
                if (index < 0) {
                    unchanged = false;
                    continue;
                }
                int size = shapes.get(j).dim(index);
                if (size == max) {
                    continue;
                }
                if (size == 1) {
                    unchanged = false;
                    continue;
                }
                return new ElementWise(false, true);
            }
        }
        return new ElementWise(true, unchanged, Shape.of(dims));
    }

    public record ElementWise(boolean valid, boolean unchanged, Shape shape) {

        public ElementWise(boolean valid, boolean unchanged) {
            this(valid, unchanged, Shape.of());
        }

        public boolean hasShape(NArray<?> t) {
            return shape.equals(t.shape());
        }

        public <N extends Number> NArray<N> transform(NArray<N> t) {
            if (hasShape(t)) {
                return t;
            }
            for (int i = 1; i <= shape.rank(); i++) {
                int index = t.rank() - i;
                if (index < 0) {
                    t = t.strexp(0, shape.dim(-i));
                    continue;
                }
                int dim = t.dim(index);
                if (dim == shape.dim(-i)) {
                    continue;
                }
                if (dim == 1) {
                    t = t.expand(index, shape.dim(-i));
                    continue;
                }
                throw new IllegalArgumentException("NArray not compatible for broadcasting.");
            }
            return t;
        }

        @Override
        public String toString() {
            return String.format("ElementWise[valid=%b, unchanged=%b, shape=%s]", valid, unchanged, shape.toString());
        }
    }
}
