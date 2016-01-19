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

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/7/15.
 */
public class EigenPair {

    private RV values;
    private RM vectors;

    private EigenPair(RV values, RM vectors) {
        this.values = values;
        this.vectors = vectors;
    }

    public static EigenPair newFrom(RV values, RM vectors) {
        return new EigenPair(values, vectors);
    }

    public RV values() {
        return values;
    }

    public RM vectors() {
        return vectors;
    }

    public RM expandedValues() {
        RM full = RM.empty(values.rowCount(), values.rowCount());
        for (int i = 0; i < values.rowCount(); i++) {
            full.set(i, i, values.get(i));
        }
        return full;
    }

    public RV vector(int i) {
        return vectors.mapCol(i);
    }
}
