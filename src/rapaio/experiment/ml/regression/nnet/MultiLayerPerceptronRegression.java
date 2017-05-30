/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.experiment.ml.regression.nnet;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;

import java.util.Arrays;

import static rapaio.sys.WS.formatFlex;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class MultiLayerPerceptronRegression extends AbstractRegression {

    private final int[] layerSizes;
    private final NetNode[][] net;
    int runs = 0;
    private TFunction function = TFunction.SIGMOID;
    private double learningRate = 1.0;

    public MultiLayerPerceptronRegression(int... layerSizes) {
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
    public Regression newInstance() {
        return new MultiLayerPerceptronRegression(layerSizes);
    }

    @Override
    public String name() {
        return "MultiLayerPerceptronRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("function=").append(function.name()).append(", ");
        sb.append("learningRate=").append(formatFlex(learningRate)).append(", ");
        sb.append("layerSizes=").append(Arrays.deepToString(Arrays.stream(layerSizes).mapToObj(i -> i).toArray()));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VarType.NUMERIC, VarType.INDEX, VarType.BINARY, VarType.ORDINAL)
                .withTargetTypes(VarType.NUMERIC)
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1_000_000)
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(false);
    }

    public MultiLayerPerceptronRegression withFunction(TFunction function) {
        this.function = function;
        return this;
    }

    public MultiLayerPerceptronRegression withLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public MultiLayerPerceptronRegression withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        for (String varName : df.getVarNames()) {
            if (df.getVar(varName).getType().isNominal()) {
                throw new IllegalArgumentException("perceptrons can't train nominal features");
            }
        }

        // validate

        if (this.targetNames().length != net[net.length - 1].length) {
            throw new IllegalArgumentException("target var names does not fit output nodes");
        }
        if (inputNames().length != net[0].length - 1) {
            throw new IllegalArgumentException("input var names does not fit input nodes");
        }

        // learn network

        int pos;
        for (int kk = 0; kk < runs; kk++) {
            pos = RandomSource.nextInt(df.getRowCount());

            // set inputs
            for (int i = 0; i < inputNames().length; i++) {
                if (df.isMissing(pos, inputName(i))) {
                    throw new RuntimeException("detected NaN in input values");
                }
                net[0][i + 1].value = df.getValue(pos, inputName(i));
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
                double expected = df.getValue(pos, targetName(i));
                double actual = net[last][i].value;
                net[last][i].gamma = function.differential(actual) * (expected - actual);
            }
            for (int i = last - 1; i > 0; i--) {
                for (int j = 0; j < net[i].length; j++) {
                    double sum = 0;
                    for (int k = 0; k < net[i + 1].length; k++) {
                        if (net[i + 1][k].weights == null) continue;
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
    protected RFit coreFit(final Frame df, final boolean withResiduals) {
        RFit pred = RFit.build(this, df, withResiduals);
        for (int pos = 0; pos < df.getRowCount(); pos++) {

            // set inputs
            for (int i = 0; i < inputNames().length; i++) {
                net[0][i + 1].value = df.getValue(pos, inputName(i));
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
                pred.fit(targetName(i)).setValue(pos, net[net.length - 1][i].value);
            }
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String getSummary() {
        throw new IllegalArgumentException("not implemented");
    }
}

@Deprecated
class NetNode {

    double value = RandomSource.nextDouble() / 10.;
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
            weights[i] = RandomSource.nextDouble() / 10.;
        }
    }
}