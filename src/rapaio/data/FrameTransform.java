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

import java.util.ArrayList;
import java.util.List;

import rapaio.data.filter.FFilter;
import rapaio.ml.common.ListParam;

/**
 * Frame transformation composed from a list of frame filters {@link FFilter}.
 * <p>
 * Collects the list of transformation which can be applied
 * over a frame. A frame filter has two lifecycle phases: fit and apply. In the fit phase the
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
 * <p>
 * The list of transformation can be configured using the {@link #filters} parameters using
 * methods like {@link ListParam#add}, {@link ListParam#clear}.
 */
public final class FrameTransform {

    public static FrameTransform newTransform(FFilter... filters) {
        var transform = new FrameTransform();
        transform.clear();
        for (var filter : filters) {
            transform.add(filter);
        }
        return transform;
    }

    private final List<FFilter> filters = new ArrayList<>();
    private boolean fitted = false;

    private FrameTransform() {
    }

    public FrameTransform newInstance() {
        FrameTransform copy = FrameTransform.newTransform();
        for (var ffilter : filters) {
            copy.add(ffilter.newInstance());
        }
        return copy;
    }

    public FrameTransform add(FFilter filter) {
        filters.add(filter);
        return this;
    }

    public FrameTransform clear() {
        filters.clear();
        return this;
    }

    public List<FFilter> filters() {
        return filters;
    }

    public Frame apply(Frame df) {
        if(!fitted) {
            throw new IllegalStateException("Transformation not fitted on data before applying it.");
        }
        Frame result = df;
        for (FFilter ff : filters) {
            result = ff.apply(result);
        }
        return result;
    }

    public Frame fapply(Frame df) {
        Frame result = df;
        for (FFilter ff : filters) {
            result = ff.fapply(result);
        }
        fitted = true;
        return result;
    }
}
