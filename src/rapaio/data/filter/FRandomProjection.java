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

package rapaio.data.filter;

import rapaio.core.SamplingTools;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.filter.ffilter.AbstractFFilter;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.math.linear.dense.SolidDVector;

import java.util.stream.IntStream;

/**
 * Builds a random projection of some give numeric features.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/28/16.
 */
public class FRandomProjection extends AbstractFFilter {

    public static FRandomProjection newGaussianSd(int k, VRange vRange) {
        return new FRandomProjection(k, gaussian(k), vRange);
    }

    public static FRandomProjection newAchlioptas(int k, VRange vRange) {
        return new FRandomProjection(k, achlioptas(3), vRange);
    }

    public static FRandomProjection newAchlioptas(int k, double s, VRange vRange) {
        return new FRandomProjection(k, achlioptas(s), vRange);
    }

    private static final long serialVersionUID = -2790372378136065870L;

    private final int k;
    private final Method method;
    private DMatrix rp;

    private FRandomProjection(int k, Method method, VRange vRange) {
        super(vRange);
        this.k = k;
        this.method = method;
    }

    @Override
    public FRandomProjection newInstance() {
        return new FRandomProjection(k, method, vRange);
    }

    @Override
    public void coreFit(Frame df) {
        // build k random projections

        rp = SolidDMatrix.empty(varNames.length, k);
        for (int i = 0; i < k; i++) {
            DVector v = method.projection(varNames.length);
            for (int j = 0; j < varNames.length; j++) {
                rp.set(j, i, v.get(j));
            }
        }
    }

    @Override
    public Frame apply(Frame df) {

        DMatrix X = SolidDMatrix.copy(df.mapVars(varNames));
        DMatrix p = X.dot(rp);

        Frame non = df.removeVars(VRange.of(varNames));
        Frame trans = SolidFrame.matrix(p, IntStream.range(1, k + 1).boxed().map(i -> "RP_" + i).toArray(String[]::new));
        return non.bindVars(trans);
    }

    public interface Method {
        DVector projection(int rowCount);
    }

    private static Method gaussian(int k) {
        return rowCount -> {
            Normal norm = Normal.std();
            DVector v = SolidDVector.zeros(rowCount);
            for (int i = 0; i < v.size(); i++) {
                v.set(i, norm.sampleNext() / Math.sqrt(k));
            }
            v.normalize(2);
            return v;
        };
    }

    private static Method achlioptas(double s) {
        double[] p = new double[3];
        p[0] = 1 / (2 * s);
        p[1] = 1 - 1 / s;
        p[2] = 1 / (2 * s);

        double sqrt = Math.sqrt(s);

        return rowCount -> {
            int[] sample = SamplingTools.sampleWeightedWR(rowCount, p);
            DVector v = SolidDVector.zeros(rowCount);
            for (int i = 0; i < sample.length; i++) {
                if (sample[i] == 0) {
                    v.set(i, -sqrt);
                    continue;
                }
                if (sample[i] == 1) {
                    v.set(i, 0);
                    continue;
                }
                v.set(i, sqrt);
            }
            return v.normalize(2);
        };
    }
}
