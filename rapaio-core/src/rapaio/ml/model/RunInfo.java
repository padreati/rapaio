/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model;

public class RunInfo<M> {

    public static <M extends ClassifierModel<M, ?, ? extends RunInfo<M>>> RunInfo<M> forClassifier(M model, int run) {
        return new RunInfo<>(model, run);
    }

    public static <M extends RegressionModel<M, ?, ? extends RunInfo<M>>> RunInfo<M> forRegression(M model, int run) {
        return new RunInfo<>(model, run);
    }

    public static <M extends ClusteringModel<M, ?, ? extends RunInfo<M>>> RunInfo<M> forClustering(M model, int run) {
        return new RunInfo<>(model, run);
    }

    private final M model;
    private final int run;

    public RunInfo(M model, int run) {
        this.model = model;
        this.run = run;
    }

    public M model() {
        return model;
    }

    public int run() {
        return run;
    }
}
