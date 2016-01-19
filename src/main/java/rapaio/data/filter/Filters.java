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

package rapaio.data.filter;

import rapaio.core.distributions.Distribution;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.stream.VSpot;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public class Filters {

    /**
     * Adds random noise normally distributed (mean=0, sd=0.1) to
     * a numerical variable.
     */
    public static Var jitter(Var x) {
        return new VFJitter().fitApply(x);
    }

    /**
     * Adds random noise normally distributed with mean=0 and specified sd to
     * a numerical variable.
     */
    public static Var jitter(Var x, double sd) {
        return new VFJitter(sd).fitApply(x);
    }

    /**
     * Adds random noise distributed according with given distribution to
     * a numerical variable.
     */
    public static Var jitter(Var x, Distribution d) {
        return new VFJitter(d).fitApply(x);
    }

    public static Frame refSort(Frame df, Comparator<Integer> comp) {
        return new FFRefSort(comp).filter(df);
    }

    /**
     * Sorts a variable using a default ascending sorting on labels
     * or numeric values.
     */
    public static Var sort(Var x) {
        return new VFSort().fitApply(x);
    }

    /**
     * Sorts a variable using a default sorting on labels
     * or numeric values.
     */
    public static Var sort(Var x, boolean asc) {
        return new VFSort(asc).fitApply(x);
    }

    @SafeVarargs
    public static Var refSort(Var x, Comparator<Integer>... comp) {
        return new VFRefSort(comp).fitApply(x);
    }

    public static Var refSort(Var x, Var ref) {
        return new VFRefSort(ref.refComparator()).fitApply(x);
    }

    public static Var refSort(Var x, Var ref, boolean asc) {
        return new VFRefSort(ref.refComparator(asc)).fitApply(x);
    }

    public static Frame refSort(Frame df, Comparator<Integer>... comp) {
        return new FFRefSort(comp).filter(df);
    }

    public static Var shuffle(Var x) {
        return new VFShuffle().fitApply(x);
    }

    public static Frame shuffle(Frame x) {
        return new FFShuffle().filter(x);
    }

    public static Var transformPower(Var x, double lambda) {
        return new VFTransformPower(lambda).fitApply(x);
    }

    public static Var transformBoxCox(Var x, double lambda) {
        return new VFTransformBoxCox(lambda).fitApply(x);
    }

    public static Var transformBoxCox(Var x, double lambda, double shift) {
        return new VFTransformBoxCox(lambda, shift).fitApply(x);
    }

    public static Var updateValue(Function<Double, Double> f, Var x) {
        return new VFUpdateValue(f).fitApply(x);
    }

    public static Var update(Consumer<VSpot> c, Var v) {
        return new VFUpdate(c).fitApply(v);
    }
}
