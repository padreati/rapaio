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

package rapaio.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rapaio.data.preprocessing.Transform;

/**
 * Frame transformation composed from a chain of frame filters {@link Transform}.
 * <p>
 * Collects the list of transformation which can be applied
 * over a frame. A frame transform has two lifecycle phases: fit and apply. In the fit phase the
 * filter learns the eventual parameters of transformation from frame data. In the {@link #apply(Frame)}  phase
 * it produce changes to the frame according to the learned parameters. If one wants to pass through
 * both phases it can use {@link #fapply(Frame)} method.
 * <p>
 * Frame transformation can be serialized and used later.
 * <p>
 * In the common scenario when one prepares data for learning and predicting the usual scenario is the following:
 * <ul>
 *     <li>fit</li> training data
 *     <li>apply</li> on training data and fit the model
 *     <li>apply</li> the same transformation to any test data set and do predictions
 * </ul>
 * The first two steps can be applied also using {@link #fapply(Frame)} to make a single step.
 */
public final class Preprocessing implements Serializable {

    public static Preprocessing newProcess(Transform... filters) {
        var transform = new Preprocessing();
        transform.clear();
        for (var filter : filters) {
            transform.add(filter);
        }
        return transform;
    }

    private final List<Transform> transformations = new ArrayList<>();
    private boolean fitted = false;

    private Preprocessing() {
    }

    public Preprocessing newInstance() {
        Preprocessing copy = Preprocessing.newProcess();
        for (var transform : transformations) {
            copy.add(transform.newInstance());
        }
        return copy;
    }

    public Preprocessing add(Transform filter) {
        transformations.add(filter);
        return this;
    }

    public Preprocessing clear() {
        transformations.clear();
        return this;
    }

    public List<Transform> transformers() {
        return transformations;
    }

    public Frame fapply(Frame df) {
        Frame result = df;
        for (var ff : transformations) {
            result = ff.fapply(result);
        }
        fitted = true;
        return result;
    }

    public Frame apply(Frame df) {
        if(!fitted) {
            throw new IllegalStateException("Transformation not fitted on data before applying it.");
        }
        Frame result = df;
        for (var ff : transformations) {
            result = ff.apply(result);
        }
        return result;
    }
}
