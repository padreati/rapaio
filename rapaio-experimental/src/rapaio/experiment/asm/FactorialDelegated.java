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

package rapaio.experiment.asm;

import java.util.stream.Stream;

public class FactorialDelegated implements FactorialInterface {

    @Override
    public long fact(int n) {
        return FactorialImpl.factorialTailRec(1L, n).invoke();
    }
}

@FunctionalInterface
interface TailCall<T> {
    TailCall<T> apply();

    default boolean isComplete() {
        return false;
    }
    default T result() {
        throw new Error("not implemented");
    }
    default T invoke() {
        return Stream.iterate(this, TailCall::apply)
                .filter(TailCall::isComplete)
                .findFirst()
                .get()
                .result();
    }
}

class FactorialImpl {
    public static TailCall<Long> factorialTailRec(final Long factorial, final int number) {
        if (number == 1) {
            return TailCalls.done(factorial);
        } else {
            return () -> factorialTailRec(factorial * number, number - 1);
        }
    }
}

class TailCalls {

    public static <T> TailCall<T> done(final T value) {
        return new TailCall<T>() {
            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }

            @Override
            public TailCall<T> apply() {
                throw new Error("not implemented");
            }
        };
    }
}