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

package rapaio.util;

import static rapaio.printer.format.Format.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/29/18.
 */
public class DoublePair {
    public double p1;
    public double p2;

    protected DoublePair(double p1, double p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public static DoublePair from(double u, double v) {
        return new DoublePair(u, v);
    }

    @Override
    public String toString() {
        return "Pair{" + floatFlex(p1) + ", " + floatFlex(p2) + "}";
    }


    public void update(double p1, double p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void update(DoublePair p) {
        this.p1 = p.p1;
        this.p2 = p.p2;
    }
}
