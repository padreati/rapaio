package rapaio.ml.classifier.tree.ctree;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.common.predicate.RowPredicate;
import rapaio.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/11/20.
 */
public enum Splitter implements Serializable {

    /**
     * Simply ignores the missing values, it will propagate only the instances which are accepted by a rule
     */
    Ignore {
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> p) {
            List<Mapping> mappings = new ArrayList<>(p.size());
            for (int i = 0; i < p.size(); i++) {
                mappings.add(Mapping.empty());
            }

            for (int row = 0; row < df.rowCount(); row++) {
                for (int i = 0; i < p.size(); i++) {
                    if (p.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        break;
                    }
                }
            }
            return Pair.from(
                    mappings.stream().map(df::mapRows).collect(toList()),
                    mappings.stream().map(weights::mapRows).collect(toList())
            );
        }
    },
    Majority {
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> p) {
            List<Mapping> mappings = new ArrayList<>(p.size());
            for (int i = 0; i < p.size(); i++) {
                mappings.add(Mapping.empty());
            }

            Mapping missingSpots = Mapping.empty();
            for (int row = 0; row < df.rowCount(); row++) {
                boolean consumed = false;
                for (int i = 0; i < p.size(); i++) {
                    if (p.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        consumed = true;
                        break;
                    }
                }
                if (!consumed)
                    missingSpots.add(row);
            }
            List<Integer> lens = mappings.stream().map(Mapping::size).collect(toList());
            Collections.shuffle(lens);
            int majorityGroup = 0;
            int majoritySize = 0;
            for (int i = 0; i < mappings.size(); i++) {
                if (mappings.get(i).size() > majoritySize) {
                    majorityGroup = i;
                    majoritySize = mappings.get(i).size();
                }
            }
            final int index = majorityGroup;

            mappings.get(index).addAll(missingSpots.iterator());

            return Pair.from(
                    mappings.stream().map(df::mapRows).collect(toList()),
                    mappings.stream().map(weights::mapRows).collect(toList())
            );
        }
    },
    /**
     * Put instances with missing value on test variable to all branches, with diminished weights
     */
    Weighted {
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> pred) {

            List<Mapping> mappings = new ArrayList<>();
            List<Var> weighting = new ArrayList<>();
            for (int i = 0; i < pred.size(); i++) {
                mappings.add(Mapping.empty());
                weighting.add(VarDouble.empty());
            }

            final double[] p = new double[mappings.size()];
            double psum = 0;
            List<Integer> missingRows = new ArrayList<>();
            for (int row = 0; row < df.rowCount(); row++) {
                boolean consumed = false;
                for (int i = 0; i < pred.size(); i++) {
                    if (pred.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        weighting.get(i).addDouble(weights.getDouble(row));
                        p[i] += weights.getDouble(row);
                        psum += weights.getDouble(row);
                        consumed = true;
                        break;
                    }
                }
                if (!consumed)
                    missingRows.add(row);
            }
            for (int i = 0; i < p.length; i++) {
                p[i] /= psum;
            }
            for (int i = 0; i < mappings.size(); i++) {
                for (int row : missingRows) {
                    // we distribute something to a node only if it has
                    // already something
                    if (p[i] > 0) {
                        mappings.get(i).add(row);
                        weighting.get(i).addDouble(weights.getDouble(row) * p[i]);
                    }
                }
            }
            List<Frame> frames = mappings.stream().map(df::mapRows).collect(toList());
            return Pair.from(frames, weighting);
        }
    },
    /**
     * Assign randomly to any child the instances with missing value on test variable
     */
    Random {
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> pred) {
            // first we collect the prediction category for each observation
            // and the counts from each category,
            // missing values are placed randomly
            int[] to = new int[df.rowCount()];
            int[] counts = new int[pred.size()];
            for (int row = 0; row < df.rowCount(); row++) {
                boolean consumed = false;
                for (int i = 0; i < pred.size(); i++) {
                    if (pred.get(i).test(row, df)) {
                        to[row] = i;
                        counts[i]++;
                        consumed = true;
                        break;
                    }
                }
                if (!consumed) {
                    int next = RandomSource.nextInt(pred.size());
                    to[row] = next;
                    counts[next]++;
                }
            }
            // we build arrays for each category of proper size
            int[][] maps = new int[pred.size()][];
            for (int i = 0; i < pred.size(); i++) {
                maps[i] = new int[counts[i]];
            }
            // here we maintain the position in each mapping
            int[] pos = new int[pred.size()];
            // fill the mappings
            for (int i = 0; i < to.length; i++) {
                int t = to[i];
                maps[t][pos[t]] = i;
                pos[t]++;
            }
            // and split the observations
            List<Frame> frameList = new ArrayList<>();
            List<Var> weightList = new ArrayList<>();
            for (int i = 0; i < pred.size(); i++) {
                frameList.add(df.mapRows(maps[i]));
                weightList.add(weights.mapRows(maps[i]));
            }
            return Pair.from(frameList, weightList);
        }
    };

    /**
     * Splits the initial data set into pairs of frame and weights according with the
     * policy for missing values implemented splitter.
     *
     * @param df         initial data set
     * @param weights    initial weights
     * @param predicates rules/criteria used to perform the splitting
     * @return a pair with a list of frames and a list of weights
     */
    public abstract Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> predicates);


}
