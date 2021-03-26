/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.math.linear;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/12/21.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TOptions {

    public static final int DEFAULT_RESULT = TOptionResult.INPLACE;
    public static final int DEFAULT_AXIS = 0;

    private int result = DEFAULT_RESULT;
    private int axis = DEFAULT_AXIS;

    public static TOptionResult inplace() {
        return new TOptionResult(TOptionResult.INPLACE);
    }

    public static TOptionResult copy() {
        return new TOptionResult(TOptionResult.COPY);
    }

    public static TOptionResult add() {
        return new TOptionResult(TOptionResult.ADD);
    }

    /**
     * Set operation axis. Possible values are:
     * <ul>
     *     <li><i>0</i>: apply operation on rows (or first dimension)</li>
     *     <li><i>1</i>: apply operation on columns (or second dimension)</li>
     *     <li><i>n>1</i>: apply operation the <i>n</i>-th dimension</li>
     *     <li><i>-1</i>: apply operation elementwise</li>
     * </ul>
     *
     * @param axis integer value which specifies axis on which to apply the options
     * @return axis option
     */
    public TOptionAxis axis(int axis) {
        return new TOptionAxis(axis);
    }

    @RequiredArgsConstructor
    private static final class TOptionResult implements TOption<Integer> {

        public static final int INPLACE = 0;
        public static final int COPY = 1;
        public static final int ADD = 2;

        private final int result;

        @Override
        public void bind(TOptions options) {
            options.result = result;
        }

        public boolean isInplace() {
            return result == INPLACE;
        }

        public boolean isCopy() {
            return result == COPY;
        }

        public boolean isAdd() {
            return result == ADD;
        }
    }

    @RequiredArgsConstructor
    private static class TOptionAxis implements TOption<Integer> {
        private final int axis;

        @Override
        public void bind(TOptions options) {
            options.axis = axis;
        }
    }

    public static TOptions bind(TOption<?>... options) {
        TOptions optionSet = new TOptions();
        for (var opt : options) {
            opt.bind(optionSet);
        }
        return optionSet;
    }
}
