/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.math.linear.impl;

import rapaio.math.linear.RM;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
@Deprecated
public class TransposeRM implements RM {
    private final RM ref;

    public TransposeRM(RM ref) {
        this.ref = ref;
    }

    @Override
    public int rowCount() {
        return ref.colCount();
    }

    @Override
    public int colCount() {
        return ref.rowCount();
    }

    @Override
    public double get(int i, int j) {
        return ref.get(j, i);
    }

    @Override
    public void set(int i, int j, double value) {
        ref.set(j, i, value);
    }

    @Override
    public RM t() {
        return ref;
    }
}
