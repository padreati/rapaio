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

package rapaio.experiment.ml.feature.generator;

import rapaio.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/18.
 */
public abstract class AbstractFeatureGroupGenerator implements FeatureGroupGenerator {

    protected final VRange range;

    protected AbstractFeatureGroupGenerator(VRange range) {
        this.range = range;
    }

    @Override
    public VRange getVRange() {
        return range;
    }

    final static class Key implements Comparable<Key> {

        public static Key from(int row, Frame df, List<String> keys) {
            Key key = new Key();
            for(String keyName : keys) {
                key.values.add(df.getLabel(row, keyName));
            }
            return key;
        }

        private List<String> values = new ArrayList<>();

        @Override
        public int compareTo(Key o) {
            int len = Math.min(values.size(), o.values.size());
            for (int i = 0; i < len; i++) {
                int compare = values.get(0).compareTo(o.values.get(0));
                if(compare!=0) {
                    return compare;
                }
            }
            return Integer.compare(values.size(), o.values.size());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key))
                return false;

            boolean equals = true;
            Key ref = (Key) obj;
            return compareTo(ref) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(values);
        }
    }
}
