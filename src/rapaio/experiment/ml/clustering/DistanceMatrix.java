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

package rapaio.experiment.ml.clustering;

import rapaio.data.Frame;
import rapaio.ml.common.distance.Distance;

import java.io.Serializable;
import java.util.stream.IntStream;

/**
 * Holds a matrix with distances between instances for a given metric.
 * It can be used to compute the distance matrix itself or just to store
 * it for later use.
 * <p>
 * The distance matrix is squared and symmetric, so for storing purposes
 * we hold only the upper triangular shape. Any update on an element
 * updates also the symmetric one, since it is stored only ne.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class DistanceMatrix implements Serializable {

    private static final long serialVersionUID = 1663354103398810554L;

    public static DistanceMatrix empty(String[] names) {
        return new DistanceMatrix(names);
    }

    public static DistanceMatrix from(Frame df, String[] varNames, Distance dist) {
        String[] names = new String[df.rowCount()];
        for (int i = 0; i < df.rowCount(); i++) {
            names[i] = "" + i;
        }
        DistanceMatrix dm = new DistanceMatrix(names);
        IntStream.range(0, df.rowCount()).parallel().forEach(i -> {
            for (int j = i + 1; j < df.rowCount(); j++) {
                dm.set(i, j, dist.compute(df, i, df, j, varNames)._1);
            }
        });
        return dm;
    }

    private String[] names;
    private double[] values;

    private DistanceMatrix(String[] names) {
        this.names = names;
        int len = names.length;
        values = new double[(len + 1) * len / 2];
    }

    public int length() {
        return names.length;
    }

    public String[] names() {
        return names;
    }

    public String name(int i) {
        return names[i];
    }

    public void set(int i, int j, double value) {
        if (j > i) {
            set(j, i, value);
            return;
        }
        int pos = i * (i + 1) / 2 + j;
        values[pos] = value;
    }

    public double get(int i, int j) {
        if (j > i)
            return get(j, i);
        int pos = i * (i + 1) / 2 + j;
        return values[pos];
    }
}
