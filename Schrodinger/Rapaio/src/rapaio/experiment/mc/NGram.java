/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.experiment.mc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/1/15.
 */
public class NGram implements ChainAdapter {

    private final int n;

    public NGram(int n) {
        this.n = n;
    }

    @Override
    public List<String> tokenize(String rawChain) {
        if (rawChain.length() < n) {
            return Collections.emptyList();
        }
        return IntStream.range(n, rawChain.length()).boxed()
                .map(i -> rawChain.substring(i - n, i))
                .collect(Collectors.toList());
    }

    @Override
    public String restore(List<String> chain) {
        StringBuilder sb = new StringBuilder();
        if (chain.size() > 0) {
            sb.append(chain.get(0));
        }
        for (int i = 1; i < chain.size(); i++) {
            String tok = chain.get(i);
            sb.append(tok.substring(tok.length() - 1));
        }
        return sb.toString();
    }
}
