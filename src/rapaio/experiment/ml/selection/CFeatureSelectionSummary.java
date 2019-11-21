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

package rapaio.experiment.ml.selection;

import rapaio.core.correlation.CorrPearson;
import rapaio.core.correlation.CorrSpearman;
import rapaio.data.Frame;
import rapaio.printer.Printable;
import rapaio.printer.format.Format;
import rapaio.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for helping on feature selection.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/22/15.
 */
public class CFeatureSelectionSummary implements Printable {

    private final String targetVar;
    private final Frame df;
    private final List<Pair<String, Double>> topPearson = new ArrayList<>();
    private final List<Pair<String, Double>> topSpearman = new ArrayList<>();
    private final List<Pair<String, Double>> topChiSquare = new ArrayList<>();
    private boolean usePearson = true;
    private boolean useSpearman = true;
    private boolean useChiSquare = true;

    public CFeatureSelectionSummary(Frame df, String targetVar) {
        this.targetVar = targetVar;
        this.df = df;
        validate();
        compute();
    }

    public CFeatureSelectionSummary withPearson(boolean usePearson) {
        this.usePearson = usePearson;
        return this;
    }

    public CFeatureSelectionSummary withSpearman(boolean useSpearman) {
        this.useSpearman = useSpearman;
        return this;
    }

    private void validate() {
        if (!df.rvar(targetVar).type().isNominal()) {
            throw new IllegalArgumentException("Target variable received as parameter does not have a nominal type");
        }
    }

    private void compute() {

        if (usePearson) {
            df.varStream()
                    .filter(v -> !v.name().equals(targetVar))
                    .forEach(v -> topPearson.add(Pair.from(v.name(), CorrPearson.of(df.rvar(targetVar), v).singleValue())));
            topPearson.sort((o1, o2) -> -Double.compare(o1._2, o2._2));
        }

        if (usePearson) {
            df.varStream()
                    .filter(v -> !v.name().equals(targetVar))
                    .forEach(v -> topSpearman.add(Pair.from(v.name(), CorrSpearman.of(df.rvar(targetVar), v).singleValue())));
            topSpearman.sort((o1, o2) -> -Double.compare(o1._2, o2._2));
        }

    }


    @Override
    public String toSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("CFeatureSelection summary\n");
        sb.append("=========================\n");
        sb.append("\n");

        if (usePearson) {
            sb.append("Pearson correlation criteria: \n");
            sb.append("\n");

            for (int i = 0; i < topPearson.size(); i++) {
                Pair<String, Double> p = topPearson.get(i);
                sb.append(String.format("%3d. %s %s\n", i + 1, p._1, Format.floatFlex(p._2)));
            }
            sb.append("\n");
        }


        if (useSpearman) {
            sb.append("Spearman correlation criteria: \n");
            sb.append("\n");

            for (int i = 0; i < topSpearman.size(); i++) {
                Pair<String, Double> p = topSpearman.get(i);
                sb.append(String.format("%3d. %s %s\n", i + 1, p._1, Format.floatFlex(p._2)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
