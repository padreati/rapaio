/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.experiment.ml.regression.boost.gbt;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.regression.Regression;

/**
 * Boosting tree regression interface.
 *
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface BTRegression extends Regression {

    @Override
    BTRegression newInstance();

    void boostFit(Frame x, Var y, Var fx, GBTLossFunction lossFunction);
}
