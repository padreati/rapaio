package rapaio.ml.model.svm;

import java.util.logging.Logger;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.linear.DMatrix;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.ClusteringModel;
import rapaio.ml.model.ClusteringResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.svm.libsvm.ModelInfo;
import rapaio.ml.model.svm.libsvm.ProblemInfo;
import rapaio.ml.model.svm.libsvm.Svm;
import rapaio.ml.model.svm.libsvm.SvmModel;

public class OneClassSvm extends ClusteringModel<OneClassSvm, ClusteringResult<OneClassSvm>, RunInfo<ClusteringResult<OneClassSvm>>> {

    private static final Logger LOGGER = Logger.getLogger(OneClassSvm.class.getName());

    public static OneClassSvm newModel() {
        return new OneClassSvm();
    }

    /**
     * Kernel function (default rbf(1)).
     */
    public final ValueParam<Kernel, OneClassSvm> kernel = new ValueParam<>(this, new RBFKernel(1), "kernel");

    /**
     * Tolerance of termination criterion (default 0.001).
     */
    public final ValueParam<Double, OneClassSvm> tolerance = new ValueParam<>(this, 0.001, "tolerance", value -> Double.isFinite(value) && value > 0);

    /**
     * Parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5).
     */
    public final ValueParam<Double, OneClassSvm> nu = new ValueParam<>(this, 0.5, "nu", v -> Double.isFinite(v) && v > 0 && v < 1);

    /**
     * Cache size in MB (default 100MB).
     */
    public final ValueParam<Long, OneClassSvm> cacheSize = new ValueParam<>(this, 100L, "cacheSize", size -> size > 0);

    /**
     * Flag for using shrinking heuristics (default true).
     */
    public final ValueParam<Boolean, OneClassSvm> shrinking = new ValueParam<>(this, true, "shrinking");

    private OneClassSvm() {
    }

    @Override
    public ClusteringModel<OneClassSvm, ClusteringResult<OneClassSvm>, RunInfo<ClusteringResult<OneClassSvm>>> newInstance() {
        return new OneClassSvm().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "OneClassSvm";
    }

    private SvmModel svm_model;
    private ProblemInfo problemInfo;
    private ModelInfo modelInfo;

    @Override
    protected OneClassSvm coreFit(Frame df, Var weights) {
        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        ProblemInfo pi = ProblemInfo.from(x, VarDouble.empty(df.rowCount()), this);
        pi.checkValidProblem();
        svm_model = Svm.svm_train(pi.computeProblem(), pi.computeParameters());
        problemInfo = pi;
        modelInfo = new ModelInfo(pi);
        return this;
    }

    @Override
    protected ClusteringResult<OneClassSvm> corePredict(Frame df, boolean withScores) {
        VarInt assign = VarInt.empty(df.rowCount()).name("clusters");
        DMatrix xs = DMatrix.copy(df.mapVars(inputNames));
        for (int i = 0; i < xs.rows(); i++) {
            double score = Svm.svm_predict(svm_model, xs.mapRow(i));
            LOGGER.finest("i:%d, score:%f".formatted(i, score));
            assign.setInt(i, score > 0 ? 1 : 0);
        }
        return new ClusteringResult<>(this, df, assign);
    }
}
