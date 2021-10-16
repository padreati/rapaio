/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.boost;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.loss.Loss;
import rapaio.ml.supervised.RegressionModel;
import rapaio.ml.supervised.RegressionResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/21/19.
 */
public abstract class GBTRtree<M extends RegressionModel<M, R, H>, R extends RegressionResult, H> extends RegressionModel<M, R, H> {

    public abstract void boostUpdate(Frame x, Var y, Var fx, Loss lossFunction);
}
