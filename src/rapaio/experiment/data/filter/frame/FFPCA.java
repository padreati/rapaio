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

package rapaio.experiment.data.filter.frame;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.ffilter.AbstractFFilter;
import rapaio.experiment.ml.analysis.PCA;
import rapaio.math.linear.DM;
import rapaio.math.linear.DV;

import java.util.function.BiFunction;

public class FFPCA extends AbstractFFilter {

    private static final long serialVersionUID = 2797285371357486124L;

    final BiFunction<DV, DM, Integer> kFun;
    private PCA pca;

    public FFPCA(BiFunction<DV, DM, Integer> kFun, VRange vRange) {
        super(vRange);
        this.kFun = kFun;
    }

    @Override
    public FFPCA newInstance() {
        return new FFPCA(kFun, vRange);
    }

    @Override
    public void coreFit(Frame df) {
        pca = new PCA();
        pca.fit(df.mapVars(varNames));
    }

    @Override
    public Frame apply(Frame df) {
        Frame rest = df.removeVars(VRange.of(varNames));
        int k = kFun.apply(pca.eigenValues(), pca.eigenVectors());
        Frame trans = pca.predict(df.mapVars(varNames), k);
        return rest.bindVars(trans);
    }
}
