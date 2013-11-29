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
package rapaio.ml.classification;

import rapaio.core.Summarizable;
import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.Vector;
import rapaio.ml.classification.colselect.ColSelector;

import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Classifier extends Summarizable {

    Classifier newInstance();

    void learn(Frame df, List<Double> weights, String classColName);

    void learn(Frame df, String classColName);

    void predict(Frame df);

    NominalVector getPrediction();

    Frame getDistribution();

}
