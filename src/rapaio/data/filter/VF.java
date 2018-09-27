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

package rapaio.data.filter;

import it.unimi.dsi.fastutil.ints.IntComparator;
import rapaio.core.distributions.Distribution;
import rapaio.data.Var;
import rapaio.data.filter.var.VFJitter;
import rapaio.data.filter.var.VFRefSort;
import rapaio.data.filter.var.VFShuffle;
import rapaio.data.filter.var.VFSort;
import rapaio.data.filter.var.VFTransformBoxCox;
import rapaio.data.filter.var.VFTransformPower;
import rapaio.data.filter.var.VFApply;
import rapaio.data.filter.var.VFApplyDouble;
import rapaio.data.stream.VSpot;

import java.util.function.Consumer;
import java.util.function.Function;

public class VF {

    /**
     * Adds random noise normally distributed (mean=0, sd=0.1) to
     * a numerical variable.
     */
    public static VFJitter jitter() {
        return new VFJitter();
    }

    /**
     * Adds random noise normally distributed with mean=0 and specified sd to
     * a numerical variable.
     */
    public static VFJitter jitter(double sd) {
        return new VFJitter(sd);
    }

    /**
     * Adds random noise distributed according with given distribution to
     * a numerical variable.
     */
    public static VFJitter jitter(Distribution d) {
        return new VFJitter(d);
    }

    /**
     * Sorts a variable using a default ascending sorting on labels
     * or numeric values.
     */
    public static VFSort sort() {
        return new VFSort();
    }

    /**
     * Sorts a variable using a default sorting on labels
     * or numeric values.
     */
    public static VFSort sort(boolean asc) {
        return new VFSort(asc);
    }

    public static VFRefSort refSort(IntComparator... comp) {
        return new VFRefSort(comp);
    }

    public static VFRefSort refSort(Var ref) {
        return new VFRefSort(ref.refComparator());
    }

    public static VFRefSort refSort(Var ref, boolean asc) {
        return new VFRefSort(ref.refComparator(asc));
    }

    public static VFShuffle shuffle() {
        return new VFShuffle();
    }

    public static VFTransformPower transformPower(double lambda) {
        return new VFTransformPower(lambda);
    }

    public static VFTransformBoxCox transformBoxCox(double lambda) {
        return new VFTransformBoxCox(lambda);
    }

    public static VFTransformBoxCox transformBoxCox(double lambda, double shift) {
        return new VFTransformBoxCox(lambda, shift);
    }

    public static VFApplyDouble applyDouble(Function<Double, Double> f) {
        return VFApplyDouble.with(f);
    }

    public static VFApply apply(Consumer<VSpot> c) {
        return VFApply.with(c);
    }
}
