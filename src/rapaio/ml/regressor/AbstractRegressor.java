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

package rapaio.ml.regressor;

import rapaio.ml.common.VarSelector;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public abstract class AbstractRegressor implements Regressor {

    protected VarSelector varSelector = new VarSelector.Standard();
    protected String[] targetNames;

    @Override
    public VarSelector getVarSelector() {
        return varSelector;
    }

    @Override
    public Regressor withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }
}
