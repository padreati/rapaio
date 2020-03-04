package rapaio.ml.eval.split;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.util.collection.IntArrays;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class KFold implements SplitStrategy {

    private final int rounds;
    private final int folds;

    public KFold(int folds) {
        this(1, folds);
    }

    public KFold(int rounds, int folds) {
        this.rounds = rounds;
        this.folds = folds;
    }

    @Override
    public List<Split> generateSplits(Frame df, Var weights, String targetName) {
        if (folds > df.rowCount()) {
            throw new IllegalArgumentException("Cannot generate more folds than actual number of observations.");
        }

        List<Split> splits = new ArrayList<>();
        for (int round = 0; round < rounds; round++) {

            // allocate mappings, one for each fold
            Mapping[] mappings = new Mapping[folds];
            for (int i = 0; i < folds; i++) {
                mappings[i] = Mapping.empty();
            }

            // distribute rows in folds
            int[] rows = IntArrays.newSeq(0, df.rowCount());
            IntArrays.shuffle(rows, RandomSource.getRandom());
            int pos = 0;
            for (int row : rows) {
                mappings[pos++].add(row);
                if (pos == folds) {
                    pos = 0;
                }
            }

            // generate splits
            for (int i = 0; i < mappings.length; i++) {
                Mapping mapping = mappings[i];
                Split split = new Split(round, i,
                        targetName,
                        df.removeRows(mapping), weights.removeRows(mapping),
                        df.mapRows(mapping), weights.mapRows(mapping));
                splits.add(split);
            }

        }
        return splits;
    }
}
