package rapaio.ml.clustering;

import lombok.AllArgsConstructor;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.util.function.SBiConsumer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/31/20.
 */
public abstract class AbstractClusteringModel<M extends AbstractClusteringModel<M, R>, R extends ClusteringResult>
        extends ParamSet<M> implements ClusteringModel, Serializable {

    private static final long serialVersionUID = 1917244313225596517L;

    public final SBiConsumer<ClusteringModel, Integer> DEFAULT_RUNNING_HOOK = (m, i) -> {
    };

    /**
     * Specifies the runs / rounds of learning.
     * For various models composed of multiple sub-models
     * the runs represents often the number of sub-models.
     * <p>
     * For example for CForest the number of runs is used to specify
     * the number of decision trees to be built.
     */
    public final ValueParam<Integer, M> runs = new ValueParam<>((M) this, 1_000,
            "runs",
            "Number of iterations for iterative iterations or number of sub ensembles.",
            x -> x > 0
    );

    public final ValueParam<SBiConsumer<ClusteringModel, Integer>, M> runningHook = new ValueParam<>((M) this, DEFAULT_RUNNING_HOOK,
            "runningHook", "Running hook");

    protected String[] inputNames;
    protected VType[] inputTypes;
    protected boolean learned = false;

    @Override
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public VType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public boolean hasLearned() {
        return learned;
    }

    @Override
    public M fit(Frame df) {
        return fit(df, VarDouble.fill(df.rowCount(), 1));
    }

    @Override
    public M fit(Frame df, Var weights) {
        FitSetup fitSetup = prepareFit(df, weights);
        return (M) coreFit(fitSetup.df, fitSetup.weights);
    }

    public FitSetup prepareFit(Frame df, Var weights) {
        List<String> inputs = Arrays.asList(df.varNames());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(name -> df.rvar(name).type()).toArray(VType[]::new);

        capabilities().checkAtLearnPhase(df, weights);
        return FitSetup.valueOf(df, weights);
    }

    public abstract ClusteringModel coreFit(Frame df, Var weights);

    @Override
    public R predict(Frame df, boolean withScores) {
        return corePredict(df, withScores);
    }

    public abstract R corePredict(Frame df, boolean withScores);

    @AllArgsConstructor
    private static final class FitSetup {

        public static FitSetup valueOf(Frame df, Var weights) {
            return new FitSetup(df, weights);
        }

        public final Frame df;
        public final Var weights;
    }
}
