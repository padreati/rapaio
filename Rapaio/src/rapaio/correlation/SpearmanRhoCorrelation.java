/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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
 */

package rapaio.correlation;

import rapaio.core.Summarizable;
import rapaio.data.Frame;
import rapaio.data.NumericVector;
import rapaio.data.SortedVector;
import rapaio.data.Vector;

/**
 * Spearman's rank correlation coefficient.
 * <p/>
 * You can compute coefficient for multiple vectors at the same time.
 * <p/>
 * See:
 * http://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SpearmanRhoCorrelation implements Summarizable {
    private final Vector[] vectors;
    private final double[][] rho;

    public SpearmanRhoCorrelation(Vector[] vectors) {
        this.vectors = vectors;
        this.rho = new double[vectors.length][vectors.length];
        compute();
    }

    public SpearmanRhoCorrelation(Frame df) {
        this.vectors = new Vector[df.getColCount()];
        for (int i = 0; i < df.getColCount(); i++) {
            vectors[i] = df.getCol(i);
        }
        this.rho = new double[vectors.length][vectors.length];
        compute();
    }

    private void compute() {
        Vector[] sorted = new Vector[vectors.length];
        Vector[] indexes = new Vector[vectors.length];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = new SortedVector(vectors[i]);
            indexes[i] = new NumericVector(vectors[i].getName(), vectors[i].getRowCount());
        }

        for (int i = 0; i < sorted.length; i++) {
            Vector sort = sorted[i];
            int start = 0;
            int end = 0;
            while (end < sort.getRowCount()) {

            }
        }

    }

    @Override
    public void summary() {
    }
}
