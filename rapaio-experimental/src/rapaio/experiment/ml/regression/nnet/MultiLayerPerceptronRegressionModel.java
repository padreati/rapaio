/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.regression.nnet;

import static rapaio.printer.Format.*;

import java.io.Serial;
import java.util.Arrays;
import java.util.Random;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class MultiLayerPerceptronRegressionModel
        extends RegressionModel<MultiLayerPerceptronRegressionModel, RegressionResult, RunInfo<MultiLayerPerceptronRegressionModel>>
        implements Printable {

    @Serial
    private static final long serialVersionUID = -7855492977246862795L;
    private final int[] layerSizes;
    private final NetNode[][] net;
    int runs = 0;
    private TFunction function = TFunction.SIGMOID;
    private double learningRate = 1.0;

    public MultiLayerPerceptronRegressionModel(int... layerSizes) {
        this.layerSizes = layerSizes;

        if (layerSizes.length < 2) {
            throw new IllegalArgumentException("neural net must have at least 2 layers (including input layer)");
        }

        // build design

        net = new NetNode[layerSizes.length][];
        for (int i = 0; i < layerSizes.length; i++) {
            int add = (i != net.length - 1) ? 1 : 0;
            net[i] = new NetNode[layerSizes[i] + add];
            for (int j = 0; j < net[i].length; j++) {
                net[i][j] = new NetNode();
            }
        }

        // wire-up nodes
        for (int i = 0; i < net.length; i++) {
            if (i == 0) {
                for (int j = 0; j < net[i].length; j++) {
                    net[i][j].setInputs(null);
                    if (j == 0) {
                        net[i][j].value = 1.;
                    }
                }
                continue;
            }
            if (i == net.length - 1) {
                for (int j = 0; j < net[i].length; j++) {
                    net[i][j].setInputs(net[i - 1]);
                }
                continue;
            }

            for (int j = 0; j < net[i].length; j++) {
                if (j == 0) {
                    net[i][j].setInputs(null);
                    net[i][j].value = 1.;
                    continue;
                }
                net[i][j].setInputs(net[i - 1]);
            }
        }
    }

    @Override
    public MultiLayerPerceptronRegressionModel newInstance() {
        return new MultiLayerPerceptronRegressionModel(layerSizes);
    }

    @Override
    public String name() {
        return "MultiLayerPerceptronRegression";
    }

    @Override
    public String fullName() {
        return name() + "{"
                + "function=" + function.name() + ", "
                + "learningRate=" + floatFlex(learningRate) + ", "
                + "layerSizes=" + Arrays.deepToString(Arrays.stream(layerSizes).boxed().toArray())
                + "}";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 1_000_000, false, VarType.DOUBLE, VarType.INT, VarType.BINARY)
                .targets(1, 1_000_000, false, VarType.DOUBLE);
    }

    public MultiLayerPerceptronRegressionModel withFunction(TFunction function) {
        this.function = function;
        return this;
    }

    public MultiLayerPerceptronRegressionModel withLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public MultiLayerPerceptronRegressionModel withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        for (String varName : df.varNames()) {
            if (df.rvar(varName).type().isNominal()) {
                throw new IllegalArgumentException("perceptrons can't predict nominal features");
            }
        }

        // validate

        if (this.targetNames().length != net[net.length - 1].length) {
            throw new IllegalArgumentException("target var names does not predict output nodes");
        }
        if (inputNames().length != net[0].length - 1) {
            throw new IllegalArgumentException("input var names does not predict input nodes");
        }

        // learn network

        int pos;
        for (int kk = 0; kk < runs; kk++) {
            pos = new Random().nextInt(df.rowCount());

            // set inputs
            for (int i = 0; i < inputNames().length; i++) {
                if (df.isMissing(pos, inputName(i))) {
                    throw new RuntimeException("detected NaN in input values");
                }
                net[0][i + 1].value = df.getDouble(pos, inputName(i));
            }

            // feed forward
            for (int i = 1; i < net.length; i++) {
                for (int j = 0; j < net[i].length; j++) {
                    if (net[i][j].inputs != null) {
                        double t = 0;
                        for (int k = 0; k < net[i][j].inputs.length; k++) {
                            t += net[i][j].inputs[k].value * net[i][j].weights[k];
                        }
                        net[i][j].value = function.compute(t);
                    }
                }
            }

            // back propagate

            for (NetNode[] layer : net) {
                for (NetNode node : layer) {
                    node.gamma = 0;
                }
            }

            int last = net.length - 1;
            for (int i = 0; i < net[last].length; i++) {
                double expected = df.getDouble(pos, targetName(i));
                double actual = net[last][i].value;
                net[last][i].gamma = function.differential(actual) * (expected - actual);
            }
            for (int i = last - 1; i > 0; i--) {
                for (int j = 0; j < net[i].length; j++) {
                    double sum = 0;
                    for (int k = 0; k < net[i + 1].length; k++) {
                        if (net[i + 1][k].weights == null) {
                            continue;
                        }
                        sum += net[i + 1][k].weights[j] * net[i + 1][k].gamma;
                    }
                    net[i][j].gamma = function.differential(net[i][j].value) * sum;
                }
            }
            for (int i = net.length - 1; i > 0; i--) {
                for (int j = 0; j < net[i].length; j++) {
                    if (net[i][j].weights != null) {
                        for (int k = 0; k < net[i][j].weights.length; k++) {
                            net[i][j].weights[k] +=
                                    learningRate * net[i][j].inputs[k].value * net[i][j].gamma;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals, final double[] quantiles) {
        RegressionResult pred = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int pos = 0; pos < df.rowCount(); pos++) {

            // set inputs
            for (int i = 0; i < inputNames().length; i++) {
                net[0][i + 1].value = df.getDouble(pos, inputName(i));
            }

            // feed forward
            for (int i = 1; i < net.length; i++) {
                for (int j = 0; j < net[i].length; j++) {
                    if (net[i][j].inputs != null) {
                        double t = 0;
                        for (int k = 0; k < net[i][j].inputs.length; k++) {
                            t += net[i][j].inputs[k].value * net[i][j].weights[k];
                        }
                        net[i][j].value = function.compute(t);
                    }
                }
            }
            for (int i = 0; i < targetNames().length; i++) {
                pred.prediction(targetName(i)).setDouble(pos, net[net.length - 1][i].value);
            }
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        throw new IllegalArgumentException("not implemented");
    }
}

@Deprecated
class NetNode {

    double value = new Random().nextDouble() / 10.;
    NetNode[] inputs;
    double[] weights;
    double gamma;

    public void setInputs(NetNode[] inputs) {
        this.inputs = inputs;
        if (inputs == null) {
            this.weights = null;
            return;
        }
        this.weights = new double[inputs.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = new Random().nextDouble() / 10.;
        }
    }
}