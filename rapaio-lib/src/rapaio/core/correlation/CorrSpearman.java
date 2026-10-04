/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.core.correlation;

import java.io.Serial;
import java.util.Arrays;
import java.util.stream.IntStream;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.transform.VarRefSort;

/**
 * Spearman's rank correlation coefficient.
 * <p>
 * You can compute coefficient for multiple vectors at the same time.
 * <p>
 * See: http://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrSpearman extends AbstractCorrelation {

    @Serial
    private static final long serialVersionUID = -270091303091388587L;

    public static CorrSpearman of(Frame df) {
        return new CorrSpearman(df.varStream().toArray(Var[]::new), df.varNames());
    }

    public static CorrSpearman of(Var... vars) {
        return new CorrSpearman(vars, Arrays.stream(vars).map(Var::name).toArray(String[]::new));
    }

    private CorrSpearman(Var[] vars, String[] names) {
        super(vars, names);
        for (int i = 0; i < vars.length; i++) {
            distanceMatrix.set(i, i, 1);
            for (int j = i + 1; j < vars.length; j++) {
                distanceMatrix.set(i, j, computeCorrelationCoefficient(vars[i], vars[j]));
            }
        }
    }

    /**
     * Rank correlation of a pair of variables, computed on the rows where both values are present.
     * <p>
     * The policy for missing values is the pairwise one, the same as for {@link CorrPearson}: the coefficient of
     * two variables does not depend on which other variables are correlated with them, nor on the rows where only
     * one of them is missing. The ranks are taken after the incomplete rows are removed, since a rank is relative
     * to the values that are compared.
     */
    private double computeCorrelationCoefficient(Var x, Var y) {
        Mapping map = Mapping.wrap(IntStream.range(0, rowCount)
                .filter(row -> !(x.isMissing(row) || y.isMissing(row)))
                .toArray());
        Var[] ranks = computeRanks(new Var[] {x.mapRows(map), y.mapRows(map)});
        return CorrPearson.of(ranks).singleValue();
    }

    /**
     * Function that computes the ranks and returns an array of rank used to compute pearson on ranks
     * @param vars
     * @return Var[] : ranks
     */
    private Var[] computeRanks(Var[] vars){

        Var[] sorted = new Var[vars.length];
        Var[] ranks = new Var[vars.length];

        for (int i = 0; i < sorted.length; i++) {
            VarInt index = VarInt.seq(vars[i].size());
            sorted[i] = VarRefSort.from(RowComparators.doubleComparator(vars[i], true)).fapply(index);
            ranks[i] = VarDouble.fill(vars[i].size());
        }

        for (int i = 0; i < sorted.length; i++) {
            int start = 0;
            while (start < sorted[i].size()) {
                int end = start;
                while (end < sorted[i].size() - 1 &&
                        vars[i].getDouble(sorted[i].getInt(end)) == vars[i].getDouble(sorted[i].getInt(end + 1))) {
                    end++;
                }
                double value = 1 + (start + end) / 2.;
                for (int j = start; j <= end; j++) {
                    ranks[i].setDouble(sorted[i].getInt(j), value);
                }
                start = end + 1;
            }
        }

        return ranks;
    }

    @Override
    protected String corrName() {
        return "spearman";
    }

    @Override
    protected String corrDescription() {
        return "Spearman's rank correlation coefficient";
    }
}
