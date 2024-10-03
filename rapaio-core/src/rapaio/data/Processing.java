/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rapaio.data.transform.Transform;

/**
 * Frame transformation composed from a chain of frame filters {@link Transform}.
 * <p>
 * Collects the list of transformation which can be applied
 * over a frame. A frame transform has two lifecycle phases: fit and apply. In the fit phase the
 * filter learns the eventual parameters of transformation from frame data. In the {@link #apply(Frame)}  phase
 * it produce changes to the frame according to the learned parameters. If one wants to pass through
 * both phases it can use {@link #fitApply(Frame)} method.
 * <p>
 * Frame transformation can be serialized and used later.
 * <p>
 * In the common scenario when one prepares data for learning and predicting the usual scenario is the following:
 * <ul>
 *     <li>fit</li> training data
 *     <li>apply</li> on training data and fit the model
 *     <li>apply</li> the same transformation to any test data set and do predictions
 * </ul>
 * The first two steps can be applied also using {@link #fitApply(Frame)} to make a single step.
 */
public final class Processing implements Serializable {

    public static Processing newProcessing() {
        return new Processing();
    }

    private final List<Transform> transformations = new ArrayList<>();
    private boolean fitted = false;

    private Processing() {
    }

    public Processing newInstance() {
        Processing copy = Processing.newProcessing();
        for (var transform : transformations) {
            copy.add(transform.newInstance());
        }
        return copy;
    }

    public Processing add(Transform filter) {
        transformations.add(filter);
        return this;
    }

    public Processing clear() {
        transformations.clear();
        return this;
    }

    public List<Transform> transformers() {
        return transformations;
    }

    public Frame fitApply(Frame df) {
        Frame result = df;
        for (var ff : transformations) {
            result = ff.fitApply(result);
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
