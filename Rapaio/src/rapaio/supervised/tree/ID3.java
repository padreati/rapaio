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

package rapaio.supervised.tree;

import rapaio.data.Frame;
import rapaio.supervised.AbstractClassifier;
import rapaio.supervised.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3 extends AbstractClassifier {

    @Override
    public void learn(Frame df, int classIndex) {
        validate(df, classIndex);
    }

    @Override
    public void printModelSummary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassifierResult predict(Frame df) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void validate(Frame df, int classIndex) {
        for (int i = 0; i < df.getColCount(); i++) {
            if (!df.getCol(i).isNominal()) {
                throw new IllegalArgumentException("ID3 can handle only isNominal attributes.");
            }
            for (int j = 0; j < df.getRowCount(); j++) {
                if (df.getCol(i).isMissing(j)) {
                    throw new IllegalArgumentException("ID3 can't handle missing values");
                }
            }
        }
        if (df.getColCount() <= classIndex) {
            throw new IllegalArgumentException("Class getIndex is not valid");
        }
    }

}
