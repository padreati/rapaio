package rapaio.ml.eval.split;

import rapaio.data.Frame;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class Split {

    private final int round;
    private final int fold;
    private final String targetName;
    private final Frame trainDf;
    private final Var trainWeights;
    private final Frame testDf;
    private final Var testWeights;

    public Split(int round, int fold, String targetName, Frame trainDf, Var trainWeights, Frame testDf, Var testWeights) {
        this.round = round;
        this.fold = fold;
        this.targetName = targetName;
        this.trainDf = trainDf;
        this.trainWeights = trainWeights;
        this.testDf = testDf;
        this.testWeights = testWeights;
    }

    public int getRound() {
        return round;
    }

    public int getFold() {
        return fold;
    }

    public String getTargetName() {
        return targetName;
    }

    public Frame getTrainDf() {
        return trainDf;
    }

    public Var getTrainWeights() {
        return trainWeights;
    }

    public Frame getTestDf() {
        return testDf;
    }

    public Var getTestWeights() {
        return testWeights;
    }
}
