package rapaio.ml.eval.split;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
@AllArgsConstructor
@Getter
public class StratifiedKFold implements SplitStrategy {

    private final int rounds;
    private final int folds;
    private final String strata;

    public StratifiedKFold(int folds, String strata) {
        this(1, folds, strata);
    }

    @Override
    public List<Split> generateSplits(Frame df, Var weights) {

        List<Split> splits = new ArrayList<>();
        for (int round = 0; round < rounds; round++) {
            List<Mapping> mappings = buildStrata(df, strata);
            for (int i = 0; i < mappings.size(); i++) {
                Mapping mapping = mappings.get(i);
                splits.add(Split.builder()
                        .round(round)
                        .fold(i)
                        .trainDf(df.removeRows(mapping))
                        .trainWeights(weights == null ? null : weights.removeRows(mapping))
                        .testDf(df.mapRows(mapping))
                        .testWeights(weights == null ? null : weights.mapRows(mapping))
                        .build());
            }
        }
        return splits;
    }

    private List<Mapping> buildStrata(Frame df, String strataName) {
        List<String> dict = df.rvar(strataName).levels();
        List<Mapping> rows = dict.stream().map(name -> Mapping.empty()).collect(Collectors.toList());
        for (int i = 0; i < df.rowCount(); i++) {
            rows.get(df.getInt(i, strataName)).add(i);
        }
        Mapping shuffle = Mapping.empty();
        for (int i = 0; i < dict.size(); i++) {
            rows.get(i).shuffle();
            shuffle.addAll(rows.get(i).iterator());
        }
        List<Mapping> strata = IntStream.range(0, folds).mapToObj(i -> Mapping.empty()).collect(Collectors.toList());
        int fold = 0;
        for (int next : shuffle) {
            strata.get(fold).add(next);
            fold++;
            if (fold == folds) {
                fold = 0;
            }
        }
        return strata;
    }
}
