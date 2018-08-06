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

package rapaio.ml.classifier.bayes.estimator;

import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/18/15.
 */
public class KernelPdf implements NumericEstimator {

    private static final long serialVersionUID = 7974390604811353859L;

    private Map<String, KDE> kde = new ConcurrentHashMap<>();
    private KFunc kfunc = new KFuncGaussian();
    private double bandwidth = 0;

    public KernelPdf() {
    }

    public KernelPdf(KFunc kfunc) {
        this.kfunc = kfunc;
    }

    public KernelPdf(KFunc kfunc, double bandwidth) {
        this.kfunc = kfunc;
        this.bandwidth = bandwidth;
    }

    @Override
    public String name() {
        return "EmpiricKDE";
    }

    @Override
    public void learn(Frame df, String targetVar, String testVar) {
        kde.clear();
        df.levels(targetVar).forEach(
                classLabel -> {
                    if ("?".equals(classLabel))
                        return;
                    Frame cond = df.stream().filter(s -> classLabel.equals(s.getLabel(targetVar))).toMappedFrame();
                    Var v = cond.rvar(testVar);
                    KDE k = new KDE(v, kfunc, (bandwidth == 0) ? KDE.silvermanBandwidth(v) : bandwidth);
                    kde.put(classLabel, k);
                });
    }

    @Override
    public double cpValue(double testValue, String targetLabel) {
        return kde.get(targetLabel).pdf(testValue);
    }

    @Override
    public NumericEstimator newInstance() {
        return new KernelPdf(kfunc, bandwidth);
    }

    @Override
    public String learningInfo() {
        return name() + "{ " + kfunc.summary() + " }";
    }

}

