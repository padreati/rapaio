/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.ml_experiment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class MarkovChain {

    private final List<String> states;
    private final Map<String, Integer> revert;
    private double[] p;
    private double[][] m;

    public MarkovChain(List<String> states) {
        this.states = states;
        this.revert = new HashMap<>();
        for (int i = 0; i < states.size(); i++) {
            revert.put(states.get(i), i);
        }
    }

    public void rebuild(List<List<String>> chains) {

        // clean

        this.p = new double[states.size()];
        this.m = new double[states.size()][states.size()];

        chains.forEach(chain ->{
            p[revert.get(chain.get(0))]++;
            for (int i = 1; i < chain.size(); i++) {
                m[revert.get(chain.get(i - 1))][revert.get(chain.get(i))]++;
            }
        });

        // smooth by add-one method

        for (int i = 0; i < m.length; i++) {
            p[i]++;
            for (int j = 0; j < m.length; j++) {
                m[i][j]++;
            }
        }

        // normalization

        normalize(p);
        for(double[] row : m) normalize(row);
    }

    private void normalize(double[] p) {
        double t = Arrays.stream(p).sum();
        for (int i = 0; i < p.length; i++) {
            p[i] /= t;
        }
    }

//    public String predict(List<String> chain) {
//
//    }
}
