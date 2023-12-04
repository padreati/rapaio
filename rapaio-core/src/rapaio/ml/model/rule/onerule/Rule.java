/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.model.rule.onerule;

import java.io.Serial;
import java.io.Serializable;

import rapaio.core.tools.DensityVector;

/**
 * Base class for a rule in one rule algorithm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/15.
 */
public abstract class Rule implements Serializable {

    @Serial
    private static final long serialVersionUID = 7073304176052957223L;

    protected final String targetLevel;
    protected final DensityVector<String> dv;
    protected final boolean zeroWeight;

    public Rule(String targetLevel, DensityVector<String> dv) {
        this.targetLevel = targetLevel;
        this.dv = dv;
        this.zeroWeight = Math.abs(dv.sum()) < 1e-32;
    }

    public String getTargetClass() {
        return targetLevel;
    }

    public double getErrorCount() {
        return zeroWeight ? 0.0 : dv.sum() - dv.get(targetLevel);
    }

    public double getTotalCount() {
        return zeroWeight ? 0.0 : dv.sum();
    }

    public double getAcc() {
        return zeroWeight ? 0.0 : dv.get(targetLevel) / dv.sum();
    }

    public DensityVector<String> getDensityVector() {
        return dv;
    }
}
