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

import java.util.Comparator;

public class Filters {

    /**
     * Adds random noise normally distributed (mean=0, sd=0.1) to
     * a numerical variable.
     */
    public static Var jitter(Var x) {
        return new VFJitter().filter(x);
    }

    /**
     * Adds random noise normally distributed with mean=0 and specified sd to
     * a numerical variable.
     */
    public static Var jitter(Var x, double sd) {
        return new VFJitter(sd).filter(x);
    }

    /**
     * Adds random noise distributed according with given distribution to
     * a numerical variable.
     */
    public static Var jitter(Var x, Distribution d) {
        return new VFJitter(d).filter(x);
    }

    /**
     * Sorts a variable using a default ascending sorting on labels
     * or numeric values.
     */
    public static Var sort(Var x) {
        return new VFSort().filter(x);
    }

    /**
     * Sorts a variable using a default sorting on labels
     * or numeric values.
     */
    public static Var sort(Var x, boolean asc) {
        return new VFSort(asc).filter(x);
    }

    @SafeVarargs
    public static Var refSort(Var x, Comparator<Integer>... comp) {
        return new VFRefSort(comp).filter();
    }

    public static Var refSort(Var x, Var ref) {
        return new VFRefSort(ref.refComparator()).filter(x);
    }

    public static Var refSort(Var x, Var ref, boolean asc) {
        return new VFRefSort(ref.refComparator(asc)).filter(x);
    }

    public static Var shuffle(Var x) {
        return new VFShuffle().filter(x);
    }

    public static Frame shuffle(Frame x) {
        return new FFShuffle().filter(x);
    }

    public static Var powerTranform(Var x, double lambda) {
        return new VFPowerTrans(lambda).filter(x);
    }
}
