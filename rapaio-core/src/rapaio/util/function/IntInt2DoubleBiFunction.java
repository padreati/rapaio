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

package rapaio.util.function;

import java.util.function.ToDoubleBiFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/6/20.
 */
@FunctionalInterface
public interface IntInt2DoubleBiFunction extends ToDoubleBiFunction<Integer, Integer> {

    @Override
    @Deprecated
    default double applyAsDouble(Integer integer, Integer integer2) {
        return applyIntIntAsDouble(integer, integer2);
    }

    double applyIntIntAsDouble(int integer, int integer2);
}
