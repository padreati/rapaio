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

package rapaio.ml.model;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.printer.Printable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/31/20.
 */
public class ClusteringResult<M extends ClusteringModel<?, ?, ?>> implements Printable {

    protected final M model;
    protected final Frame df;
    protected final VarInt assignment;
    protected final VarDouble scores;

    public ClusteringResult(M model, Frame df, VarInt assignment) {
        this(model, df, assignment, null);
    }

    public ClusteringResult(M model, Frame df, VarInt assignment, VarDouble scores) {
        this.model = model;
        this.df = df;
        this.assignment = assignment;
        this.scores = scores;
    }

    public M model() {
        return model;
    }

    public Frame df() {
        return df;
    }

    public VarInt assignment() {
        return assignment;
    }

    public VarDouble scores() {
        return scores;
    }
}
