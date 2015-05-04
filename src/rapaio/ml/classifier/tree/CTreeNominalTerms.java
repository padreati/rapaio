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

package rapaio.ml.classifier.tree;

import rapaio.data.Frame;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

@Deprecated
public class CTreeNominalTerms {

    private final Map<String, Set<Integer>> indexes = new HashMap<>();

    public CTreeNominalTerms init(Frame df) {
        Arrays.stream(df.varNames())
                .map(df::var)
                .filter(var -> var.type().isNominal())
                .forEach(var -> indexes.put(var.name(), IntStream.range(1, var.dictionary().length).boxed().collect(toSet())));
        return this;
    }

    public Set<Integer> indexes(String key) {
        return indexes.get(key);
    }

    public CTreeNominalTerms solidCopy() {
        CTreeNominalTerms terms = new CTreeNominalTerms();
        this.indexes.entrySet().forEach(e -> terms.indexes.put(e.getKey(), new HashSet<>(e.getValue())));
        return terms;
    }

}
