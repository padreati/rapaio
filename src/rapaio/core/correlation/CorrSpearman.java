/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.correlation;

import java.io.Serial;
import java.util.Arrays;
import java.util.stream.IntStream;

import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.filter.VRefSort;

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
        Mapping map = Mapping.wrap(IntStream.range(0, rowCount)
                .filter(row -> {
                    for (Var var : vars) {
                        if (var.isMissing(row))
                            return false;
                    }
                    return true;
                })
                .toArray());

        Var[] variables = new Var[vars.length];
        for (int i = 0; i < vars.length; i++) {
            variables[i] = vars[i].mapRows(map);
        }
        compute(variables);
    }

    private void compute(Var[] vars) {

        Var[] ranks;

        // compute ranks
        ranks = computeRanks(vars);

        // compute Pearson on ranks
        DistanceMatrix dp = CorrPearson.of(ranks).matrix();

        for (int i = 0; i < dp.length(); i++) {
            for (int j = 0; j < dp.length(); j++) {
                distanceMatrix.set(i, j, dp.get(i, j));
            }
        }
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
            sorted[i] = new VRefSort(RowComparators.doubleComparator(vars[i], true)).fapply(index);
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
