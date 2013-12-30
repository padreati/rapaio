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
package rapaio.ml.classification.tree;

import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.ml.classification.AbstractClassifier;
import rapaio.ml.classification.Classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class CART extends AbstractClassifier{


    @Override
    public Classifier newInstance() {
        return null;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String classColName) {
    }

    @Override
    public void predict(Frame df) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NominalVector getPrediction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Frame getDistribution() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void summary() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
