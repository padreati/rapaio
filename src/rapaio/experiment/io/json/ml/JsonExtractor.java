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

package rapaio.experiment.io.json.ml;

import rapaio.data.*;
import rapaio.experiment.io.json.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class used to extract features from a JsonValue steam.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/10/15.
 */
@Deprecated
public class JsonExtractor {

    private final List<JsonFeature> features = new ArrayList<>();

    public void add(JsonFeature feat) {
        features.add(feat);
    }

    public Frame extract(Stream<JsonValue> stream) {
        stream.sequential().forEach(js -> {
            for (JsonFeature feat : features) {
                feat.apply(js);
            }
        });
        List<Var> vars = new ArrayList<>();
        for (JsonFeature feat : features) {
            vars.add(feat.getResult());
        }
        return SolidFrame.byVars(vars);
    }
}

