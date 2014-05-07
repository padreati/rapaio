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

package rapaio.ml.classifier;

import rapaio.ml.classifier.colselect.ColSelector;
import rapaio.ml.classifier.colselect.StdColSelector;
import rapaio.data.Frame;
import rapaio.data.Nominal;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    protected ColSelector colSelector = new StdColSelector();
    protected String targetCol;
    protected String[] dict;
    protected Nominal pred;
    protected Frame dist;

    @Override
    public ColSelector getColSelector() {
        return colSelector;
    }

    @Override
    public Classifier withColSelector(ColSelector colSelector) {
        this.colSelector = colSelector;
        return this;
    }

    @Override
    public String getTargetCol() {
        return targetCol;
    }

    @Override
    public String[] getDict() {
        return dict;
    }

    @Override
    public Nominal pred() {
        return pred;
    }

    @Override
    public Frame dist() {
        return dist;
    }
}
