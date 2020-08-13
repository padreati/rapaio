package rapaio.ml.classifier.tree.ctree;

import lombok.RequiredArgsConstructor;
import rapaio.core.tools.DensityVector;
import rapaio.experiment.ml.common.predicate.RowPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
@RequiredArgsConstructor
public final class Node implements Serializable {

    private static final long serialVersionUID = -5045581827808911763L;

    public final Node parent;
    public final int id;
    public final int depth;
    public final String groupName;
    public final RowPredicate predicate;

    public final List<Node> children = new ArrayList<>();
    public boolean leaf = true;
    public DensityVector<String> density;
    public DensityVector<String> counter;
    public String bestLabel;
    public Candidate bestCandidate;

    public void cut() {
        leaf = true;
        children.clear();
    }
}
