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

package rapaio.math.linear;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.util.Arrays;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public final class MathNumeric {

    public static Numeric sum(final Numeric num) {
        return Numeric.newFill(1, num.stream().mapToDouble().sum());
    }

    public static Numeric mean(final Numeric num) {
        return Numeric.newFill(1, new Mean(num).value());
    }

    public static Numeric sd(final Numeric num) {
        return Numeric.newFill(1, StrictMath.sqrt(new Variance(num).value()));
    }

    public static Numeric var(final Numeric num) {
        return Numeric.newFill(1, new Variance(num).value());
    }

    public static Numeric plus(final Numeric... nums) {
        int len = Arrays.stream(nums).mapToInt(Numeric::rowCount).min().getAsInt();
        Numeric c = Numeric.newFill(len, 0);
        for (Numeric num : nums) {
            for (int j = 0; j < len; j++) {
                c.setValue(j, c.value(j) + num.value(j));
            }
        }
        return c;
    }

    public static Numeric minus(final Numeric a, final Numeric b) {
        Numeric c = Numeric.newEmpty();
        for (int i = 0; i < StrictMath.max(a.rowCount(), b.rowCount()); i++) {
            c.addValue(a.value(i) - b.value(i));
        }
        return c;
    }

    public static Numeric dot(final Numeric a, final Numeric b) {
        final int len = StrictMath.max(a.rowCount(), b.rowCount());
        Numeric c = Numeric.newEmpty(len);
        for (int i = 0; i < len; i++) {
            c.setValue(i, a.value(i % a.rowCount()) * b.value(i % b.rowCount()));
        }
        return c;
    }

    public static Numeric dotSum(final Numeric a, final Numeric b) {
        final int len = StrictMath.max(a.rowCount(), b.rowCount());
        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum += a.value(i % a.rowCount()) * b.value(i % b.rowCount());
        }
        Numeric c = Numeric.newEmpty();
        c.addValue(sum);
        return c;
    }

    public static Numeric div(final Numeric a, final Numeric b) {
        final int len = StrictMath.max(a.rowCount(), b.rowCount());
        Numeric c = Numeric.newEmpty(len);
        for (int i = 0; i < len; i++) {
            c.setValue(i, a.value(i % a.rowCount()) / b.value(i % b.rowCount()));
        }
        return c;
    }

    public static Numeric scale(final Numeric a) {
        final Numeric v = Numeric.newEmpty(a.rowCount());
        double mean = mean(a).value(0);
        double sd = sd(a).value(0);
        for (int i = 0; i < v.rowCount(); i++) {
            v.setValue(i, (a.value(i) - mean) / sd);
        }
        return v;
    }

    public static Numeric pow(final Var a, double pow) {
        Numeric v = Numeric.newEmpty();
        for (int i = 0; i < a.rowCount(); i++) {
            v.addValue(StrictMath.pow(a.value(i), pow));
        }
        return v;
    }

    public static Numeric ln(final Var a, double shift) {
        Numeric v = Numeric.newEmpty();
        for (int i = 0; i < a.rowCount(); i++) {
            v.addValue(StrictMath.log(a.value(i) + shift));
        }
        return v;
    }

}
