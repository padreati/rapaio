package rapaio.ml.regression.tree;

import rapaio.ml.loss.Loss;
import rapaio.ml.regression.tree.rtree.SearchPayload;

/**
 * Extended loss function which can be used to find the best split for a tree.
 * The argument contains needed information required to compute the reduce in loss after
 * the split.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
public interface RTreeLoss extends Loss {

    double computeSplitLossScore(SearchPayload payload);
}
