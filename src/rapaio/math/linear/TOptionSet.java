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

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/12/21.
 */
public class TOptionSet {

    public static final TOptionResult DEFAULT_RESULT = TOptionResult.COPY;

    private TOptionResult result = DEFAULT_RESULT;

    public static TOptionResult copy() {
        return TOptionResult.COPY;
    }

    public static TOptionResult add() {
        return TOptionResult.ADD;
    }

    public enum TOptionResult implements TOption<TOptionResult> {
        INPLACE, COPY, ADD
    }

    public static TOptionSet from(TOption<?>... options) {
        TOptionSet optionSet = new TOptionSet();
        for (var opt : options) {
            optionSet.set(opt);
        }
        return optionSet;
    }

    private TOptionSet() {
    }

    private void set(TOption<?> option) {
        if (option instanceof TOptionResult) {
            result = (TOptionResult) option;
            return;
        }
    }
}
