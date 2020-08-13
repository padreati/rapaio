package rapaio.ml.classifier.tree.ctree;

import lombok.RequiredArgsConstructor;
import rapaio.experiment.ml.common.predicate.RowPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
@RequiredArgsConstructor
public final class Candidate implements Serializable {

    private static final long serialVersionUID = -1547847207988912332L;

    public final double score;
    public final String testName;
    public final List<RowPredicate> groupPredicates = new ArrayList<>();

    public void addGroup(RowPredicate predicate) {
        groupPredicates.add(predicate);
    }
}
