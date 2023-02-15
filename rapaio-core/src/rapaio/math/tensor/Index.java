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

import java.util.Arrays;

public final class Index {

    private final Shape shape;
    private final int[] sortOrder;
    private final int[] idxs;

    public Index(Shape shape, int[] sortOrder) {
        this.shape = shape;
        this.idxs = new int[shape.rank()];
        this.sortOrder = Arrays.copyOf(sortOrder, sortOrder.length);
    }

    public Index(Shape shape, Order order) {
        this.shape = shape;
        this.idxs = new int[shape.rank()];
        this.sortOrder = computeSortOrder(order);
    }

    private int[] computeSortOrder(Order order) {
        int[] so = new int[shape.rank()];
        switch (order) {
            case C -> {
                for (int i = 0; i < shape.rank(); i++) {
                    so[i] = i;
                }
            }
            case F -> {
                for (int i = 0; i < shape.rank(); i++) {
                    so[so.length - 1 - i] = i;
                }
            }
            default -> throw new IllegalArgumentException("Invalid order.");
        }
        return so;
    }

    public void next() {

    }
}
