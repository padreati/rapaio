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

package rapaio.util.comparator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/29/18.
 */
public class DoubleComparator implements it.unimi.dsi.fastutil.doubles.DoubleComparator {

    public static DoubleComparator fun = new DoubleComparator();

    @Override
    public int compare(double v1, double v2) {
        boolean nan1 = Double.isNaN(v1);
        boolean nan2 = Double.isNaN(v2);
        if (!(nan1 || nan2)) {
            if (v1 == v2) {
                return 0;
            }
            return v1 < v2 ? -1 : 1;
        }
        if (nan1 && nan2) {
            return 0;
        }
        return nan1 ? -1 : 1;
    }
}
