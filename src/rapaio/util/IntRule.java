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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.util;

import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/12/20.
 */
@FunctionalInterface
public interface IntRule {

    static IntRule none() {
        return row -> false;
    }

    static IntRule all() {
        return row -> true;
    }

    static IntRule less(int end) {
        return row -> row < end;
    }

    static IntRule greater(int start) {
        return row -> row > start;
    }

    static IntRule geq(int start) {
        return row -> row >= start;
    }

    static IntRule leq(int end) {
        return row -> row <= end;
    }

    static IntRule range(int start, int end) {
        return row -> row >= start && row < end;
    }

    static IntRule from(int... array) {
        return row -> {
            for (int r : array) {
                if (row == r) {
                    return true;
                }
            }
            return false;
        };
    }

    static IntPredicate not(IntPredicate target) {
        Objects.requireNonNull(target);
        return target.negate();
    }

    boolean test(int t);

    default IntRule and(IntRule other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default IntRule negate() {
        return (t) -> !test(t);
    }

    default IntPredicate or(IntPredicate other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }
}
