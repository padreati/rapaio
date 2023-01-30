/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.algos.edit;

import rapaio.util.param.ParamSet;
import rapaio.util.param.ValueParam;
import rapaio.util.collection.IntArrays;

/**
 * Implements various algorithms for computing edit distance between two arrays of values.
 */
public class EditDistance extends ParamSet<EditDistance> {

    public static EditDistance newDefault() {
        return new EditDistance();
    }

    public enum Method {
        WAGNER_FISCHER
    }

    public final ValueParam<Method, EditDistance> method = new ValueParam<>(this, Method.WAGNER_FISCHER, "method");

    private EditDistance() {
    }

    public <T extends Comparable<T>> int computeDistance(T[] first, T[] second) {
        int m = first.length;
        int n = second.length;

        int[][] d = new int[m + 1][n + 1];
        for (int i = 0; i < d.length; i++) {
            d[i] = IntArrays.newFill(n + 1, 0);
        }

        for (int i = 1; i < m + 1; i++) {
            d[i][0] = i;
        }
        for (int j = 1; j < n + 1; j++) {
            d[0][j] = j;
        }
        for (int i = 1; i < m + 1; i++) {
            for (int j = 1; j < n + 1; j++) {
                int cost = (first[i].compareTo(second[j]) == 0) ? 0 : 1;
                int min = Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1);
                d[i][j] = Math.min(min, d[i - 1][j - 1] + cost);
            }
        }
        return d[m][n];
    }

    public int computeDistance(int[] first, int[] second) {
        int m = first.length;
        int n = second.length;

        int[] dPrev = IntArrays.newSeq(0, n + 1);
        for (int i = 1; i < m + 1; i++) {
            int[] d = new int[n + 1];
            d[0] = i;
            for (int j = 1; j < n + 1; j++) {
                int cost = (first[i - 1] == second[j - 1]) ? 0 : 1;
                int min = Math.min(dPrev[j] + 1, d[j - 1] + 1);
                d[j] = Math.min(min, dPrev[j - 1] + cost);
            }
            System.arraycopy(d, 0, dPrev, 0, d.length);
        }
        return dPrev[n];
    }

    public int computeDistance(String first, String second) {
        return computeDistance(first.codePoints().toArray(), second.codePoints().toArray());
    }

    public static void main(String[] args) {
        EditDistance d = EditDistance.newDefault();
        int dist = d.computeDistance("sitting", "kitten");
        System.out.println(dist);
    }
}
