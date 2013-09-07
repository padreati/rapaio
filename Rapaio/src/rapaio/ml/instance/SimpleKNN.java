/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.ml.instance;

import rapaio.data.Frame;
import rapaio.supervised.AbstractClassifier;
import rapaio.supervised.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class SimpleKNN extends AbstractClassifier {

    private Frame matrix;
    private int k;
    private Frame train;
    private int classIndex;

    public Frame getMatrix() {
        return matrix;
    }

    public void setMatrix(Frame matrix) {
        this.matrix = matrix;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public void learn(Frame train, int classIndex) {
        this.train = train;
        this.classIndex = classIndex;
    }

    @Override
    public ClassifierResult predict(Frame test) {
        for (int i = 0; i < test.getRowCount(); i++) {
            for (int j = 0; j < train.getRowCount(); j++) {
                double dist = matrix.getCol(test.rowId(j)).getValue(train.rowId(i));

            }
        }
        return null;
    }

    @Override
    public void printModelSummary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
