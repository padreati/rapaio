package rapaio.ml.eval.split;

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
public class StratifiedKFold implements SplitStrategy {

    private final int rounds;
    private final int folds;

    public StratifiedKFold(int folds) {
        this(1, folds);
    }

    public StratifiedKFold(int rounds, int folds) {
        this.rounds = rounds;
        this.folds = folds;
    }

    @Override
    public List<Split> generateSplits(Frame df, Var weights, String targetName) {

        List<Split> splits = new ArrayList<>();
        for (int round = 0; round < rounds; round++) {
            List<Mapping> mappings = buildStrata(df, targetName);
            for (int i = 0; i < mappings.size(); i++) {
                Mapping mapping = mappings.get(i);
                Split split = new Split(round, i,
                        targetName,
                        df.removeRows(mapping), weights.removeRows(mapping),
                        df.mapRows(mapping), weights.mapRows(mapping));
                splits.add(split);
            }
        }
        return splits;
    }

    private List<Mapping> buildStrata(Frame df, String targetName) {
        List<String> dict = df.rvar(targetName).levels();
        List<Mapping> rows = dict.stream().map(name -> Mapping.empty()).collect(Collectors.toList());
        for (int i = 0; i < df.rowCount(); i++) {
            rows.get(df.getInt(i, targetName)).add(i);
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
