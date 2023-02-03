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
package rapaio.util;

import java.util.Comparator;

/**
 * A type-specific {@link Comparator} for double; provides methods to compare two primitive types both as objects
 * and as primitive types.
 *
 * @see Comparator
 */
@FunctionalInterface
public interface FloatComparator extends Comparator<Float> {
    /**
     * Compares its two primitive-type arguments for order. Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     *
     * @return a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     */
    int compare(float k1, float k2);

    @Override
    default FloatComparator reversed() {
        return FloatComparators.oppositeComparator(this);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation delegates to the corresponding type-specific method.
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    @Override
    default int compare(Float ok1, Float ok2) {
        return compare(ok1.floatValue(), ok2.floatValue());
    }

    /**
     * Return a new comparator that first uses this comparator, then uses the second comparator
     * if this comparator compared the two elements as equal.
     *
     * @see Comparator#thenComparing(Comparator)
     */
    default FloatComparator thenComparing(FloatComparator second) {
        return (FloatComparator & java.io.Serializable) (k1, k2) -> {
            int comp = compare(k1, k2);
            return comp == 0 ? second.compare(k1, k2) : comp;
        };
    }

    @Override
    default Comparator<Float> thenComparing(Comparator<? super Float> second) {
        if (second instanceof FloatComparator) return thenComparing((FloatComparator) second);
        return Comparator.super.thenComparing(second);
    }
}
