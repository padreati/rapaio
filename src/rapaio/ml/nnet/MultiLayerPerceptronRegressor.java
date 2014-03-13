/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.nnet;

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Vector;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MultiLayerPerceptronRegressor extends AbstractRegressor implements Serializable {

    private final double learningRate;
    private final NetNode[][] net;
    private final SigmoidFunction sigmoid = new SigmoidFunction();

    List<String> inputCols;
    List<String> targetCols;
    Frame prediction;
    int rounds = 0;

    public MultiLayerPerceptronRegressor(int[] layerSizes, double learningRate) {
        this.learningRate = learningRate;

        if (layerSizes.length < 2) {
            throw new IllegalArgumentException("neural net must hav at least 2 layers (including input layer)");
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
    public Regressor newInstance() {
        throw new RuntimeException("Not implemented");
    }

    public int getRounds() {
        return rounds;
    }

    public MultiLayerPerceptronRegressor setRounds(int rounds) {
        this.rounds = rounds;
        return this;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        this.learn(df, null, targetColName);
    }

    @Override
    public void learn(Frame df, String targetColNames) {
        ColRange targetColRange = new ColRange(targetColNames);
        List<Integer> targets = targetColRange.parseColumnIndexes(df);
        targetCols = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) {
            targetCols.add(df.getColNames()[targets.get(i)]);
        }
        inputCols = new ArrayList<>();
        for (int i = 0; i < df.getColNames().length; i++) {
            if (targetCols.contains(df.getColNames()[i])) continue;
            if (df.getCol(df.getColNames()[i]).type().isNominal()) continue;
            inputCols.add(df.getColNames()[i]);
        }

        // validate
        if (targetCols.size() != net[net.length - 1].length) {
            throw new IllegalArgumentException("target columns does not fit output nodes");
        }
        if (inputCols.size() != net[0].length - 1) {
            throw new IllegalArgumentException("input columns does not fit input nodes");
        }

        // learn network

        for (int kk = 0; kk < rounds; kk++) {
            int pos = RandomSource.nextInt(df.rowCount());

            // set inputs
            for (int i = 0; i < inputCols.size(); i++) {
                if (df.isMissing(pos, inputCols.get(i))) {
                    throw new RuntimeException("detected NaN in input values");
                }
                net[0][i + 1].value = df.getValue(pos, inputCols.get(i));
            }

            // feed forward
            for (int i = 1; i < net.length; i++) {
                for (int j = 0; j < net[i].length; j++) {
                    if (net[i][j].inputs != null) {
                        double t = 0;
                        for (int k = 0; k < net[i][j].inputs.length; k++) {
                            t += net[i][j].inputs[k].value * net[i][j].weights[k];
                        }
                        net[i][j].value = sigmoid.compute(t);
                    }
                }
            }

            // back propagate

            for (int i = 0; i < net.length; i++) {
                for (int j = 0; j < net[i].length; j++) {
                    net[i][j].gamma = 0;
                }
            }

            int last = net.length - 1;
            for (int i = 0; i < net[last].length; i++) {
                double expected = df.getValue(pos, targetCols.get(i));
                double actual = net[last][i].value;
                net[last][i].gamma = sigmoid.differential(actual) * (expected - actual);
            }
            for (int i = last - 1; i > 0; i--) {
                for (int j = 0; j < net[i].length; j++) {
                    double sum = 0;
                    for (int k = 0; k < net[i + 1].length; k++) {
                        if (net[i + 1][k].weights == null) continue;
                        sum += net[i + 1][k].weights[j] * net[i + 1][k].gamma;
                    }
                    net[i][j].gamma = sigmoid.differential(net[i][j].value) * sum;
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
    }

    public void predict(Frame df) {
        prediction = Frames.newMatrixFrame(df.rowCount(), targetCols);

        for (int pos = 0; pos < df.rowCount(); pos++) {
            // set inputs
            for (int i = 0; i < inputCols.size(); i++) {
                net[0][i + 1].value = df.getValue(pos, inputCols.get(i));
            }

            // feed forward
            for (int i = 1; i < net.length; i++) {
                for (int j = 0; j < net[i].length; j++) {
                    if (net[i][j].inputs != null) {
                        double t = 0;
                        for (int k = 0; k < net[i][j].inputs.length; k++) {
                            t += net[i][j].inputs[k].value * net[i][j].weights[k];
                        }
                        net[i][j].value = sigmoid.compute(t);
                    }
                }
            }
            for (int i = 0; i < targetCols.size(); i++) {
                prediction.setValue(pos, i, net[net.length - 1][i].value);
            }
        }
    }

    @Override
    public Vector getFitValues() {
        return prediction.getCol(0);
    }

    @Override
    public Frame getAllFitValues() {
        return prediction;
    }
}
