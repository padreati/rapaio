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

package rapaio.ml.eval;

import rapaio.data.Numeric;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.filter.var.VFCumulativeSum;
import rapaio.data.filter.var.VFRefSort;

import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/15.
 */
@Deprecated
public class NormalizedGini {

    /*
SumModelGini <- function(solution, submission) {
  df = data.frame(solution = solution, submission = submission)
  df <- df[order(df$submission, decreasing = TRUE),]
  df
  df$random = (1:nrow(df))/nrow(df)
  df
  totalPos <- sum(df$solution)
  df$cumPosFound <- cumsum(df$solution) # this will store the cumulative number of positive examples found (used for computing "Model Lorentz")
  df$Lorentz <- df$cumPosFound / totalPos # this will store the cumulative proportion of positive examples found ("Model Lorentz")
  df$Gini <- df$Lorentz - df$random # will store Lorentz minus random
  print(df)
  return(sum(df$Gini))
}

NormalizedGini <- function(solution, submission) {
  SumModelGini(solution, submission) / SumModelGini(solution, solution)
}
     */

    private final double value;

    public NormalizedGini(Var solution, Var submission) {
        value = sumModelGini(solution, submission) / sumModelGini(solution, solution);
    }

    private double sumModelGini(Var solution, Var submission) {
        Comparator<Integer> cmp = RowComparators.numeric(submission, false);
        Var sol = new VFRefSort(cmp).fitApply(solution);
        Var sub = new VFRefSort(cmp).fitApply(submission);
        int n = sub.rowCount();
        Numeric rand = IntStream.range(1, n + 1).mapToDouble(x -> x / (double) n).boxed().collect(Numeric.collector());
        double totalPos = sol.stream().mapToDouble().sum();
        Var cumPosFound = new VFCumulativeSum().fitApply(sol.solidCopy());
        Var lorentz = cumPosFound.stream().transValue(x -> x / totalPos).mapToDouble().boxed().collect(Numeric.collector());
        return IntStream.range(0, n).mapToDouble(row -> lorentz.value(row) - rand.value(row)).sum();
    }

    public double value() {
        return value;
    }
}
