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

package rapaio.data.filter.var;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.ml.regression.Regression;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/18/16.
 */
public class VFImputeWithRegression extends VFAbstract {

    private static final long serialVersionUID = -2841651242636043825L;

    public Regression model;
    public VRange inputRange;
    public String target;

    public VFImputeWithRegression(Regression model, VRange inputRange, String target) {
        this.model = model;
        this.inputRange = inputRange;
        this.target = target;
    }

    @Override
    public void fit(Var... vars) {
        if (model.hasLearned())
            return;
        Frame all = BoundFrame.newByVars(vars).mapVars(inputRange);
        Frame complete = all.stream().filter(s -> !s.missing(target)).toMappedFrame();
        model = model.newInstance().train(complete, target);
    }

    @Override
    public Var apply(Var... vars) {
        return model.fit(BoundFrame.newByVars(vars).mapVars(inputRange)).firstFit().withName(target);
    }
}
