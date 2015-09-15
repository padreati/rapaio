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

package rapaio.experiment.classifier.ensemble;

import rapaio.data.sample.FrameSampler;
import rapaio.ml.classifier.Classifier;
import rapaio.experiment.classifier.ensemble.impl.BaggingMode;
import rapaio.experiment.classifier.ensemble.impl.CEnsemble;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.common.VarSelector;

/**
 * Bagging ensemble
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
@Deprecated
public class CBagging extends CEnsemble {

    private static final long serialVersionUID = -3840736248236328445L;

    public CBagging() {
        this.runs = 10;
        this.baggingMode = BaggingMode.VOTING;
        this.oobComp = false;
        this.withSampler(new FrameSampler.Bootstrap(1));
        this.c = CTree.newC45().withVarSelector(VarSelector.ALL);
    }

    @Override
    public String name() {
        return "CBagging";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append(" {");
        sb.append("runs:").append(runs).append(",");
        sb.append("baggingMode:").append(baggingMode.name()).append(",");
        sb.append("oob:").append(oobComp).append(",");
        sb.append("sampler:").append(sampler().name()).append(",");
        sb.append("weak:").append(c.fullName()).append("");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public CBagging withRuns(int runs) {
        return (CBagging) super.withRuns(runs);
    }

    @Override
    public CBagging withOobComp(boolean oobCompute) {
        return (CBagging) super.withOobComp(oobCompute);
    }

    @Override
    public CBagging withBaggingMode(BaggingMode baggingMode) {
        return (CBagging) super.withBaggingMode(baggingMode);
    }

    public CBagging withBootstrap() {
        return withSampler(new FrameSampler.Bootstrap(1));
    }

    public CBagging withBootstrap(double p) {
        return withSampler(new FrameSampler.Bootstrap(p));
    }

    public CBagging withNoSampling() {
        return withSampler(new FrameSampler.Identity());
    }

    @Override
    public CBagging withSampler(FrameSampler sampler) {
        return (CBagging) super.withSampler(sampler);
    }

    public CBagging withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    @Override
    public CBagging newInstance() {
        return new CBagging()
                .withRuns(runs)
                .withBaggingMode(BaggingMode.VOTING);
    }

}
