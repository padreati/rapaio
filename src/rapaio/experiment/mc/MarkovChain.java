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

package rapaio.experiment.mc;

import rapaio.core.RandomSource;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;
import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class MarkovChain implements Printable {

    private List<String> states;
    private Map<String, Integer> revert;
    private RV p;
    private RM m;
    private ChainAdapter adapter = new NGram(2);
    //
    private double smoothEps = 1e-30;

    public MarkovChain() {
    }

    private static String clean(String text) {
        text = text.replace(')', ' ');
        text = text.replace('(', ' ');
        text = text.replace(';', ' ');
        text = text.replace('\'', ' ');
        text = text.replace(':', ' ');
        text = text.replace('/', ' ');

        text = text.replace('1', ' ');
        text = text.replace('2', ' ');
        text = text.replace('3', ' ');
        text = text.replace('4', ' ');
        text = text.replace('5', ' ');
        text = text.replace('6', ' ');
        text = text.replace('7', ' ');
        text = text.replace('8', ' ');
        text = text.replace('9', ' ');
        text = text.replace('0', ' ');

        text = text.replace('!', '.');
        text = text.replace('?', '.');

        return text.toLowerCase().trim() + ".";
    }

    public MarkovChain withAdapter(ChainAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public void train(List<String> rowChains) {

        this.states = new ArrayList<>(rowChains.stream().flatMap(chain -> adapter.tokenize(chain).stream()).collect(Collectors.toSet()));
        this.revert = new HashMap<>();
        for (int i = 0; i < states.size(); i++) {
            revert.put(states.get(i), i);
        }

        // clean

        this.p = SolidRV.fill(states.size(), smoothEps);
        this.m = SolidRM.fill(states.size(), states.size(), smoothEps);

        List<List<String>> chains = rowChains.stream()
                .map(chain -> adapter.tokenize(chain))
                .filter(chain -> !chain.isEmpty())
                .collect(Collectors.toList());

        for (List<String> chain : chains) {
            if (chain.isEmpty())
                continue;

            p.increment(revert.get(chain.get(0)), 1);
            for (int i = 1; i < chain.size(); i++) {
                m.increment(revert.get(chain.get(i - 1)), revert.get(chain.get(i)), 1.0);
            }
        }

        // normalization
        p.normalize(1);
        for (int i = 0; i < m.rowCount(); i++) {
            m.mapRow(i).normalize(1);
        }
    }

    public List<String> generateChain(Predicate<List<String>> tokenCondition) {
        List<String> result = new ArrayList<>();

        double c = RandomSource.nextDouble();

        int last = -1;
        for (int i = 0; i < p.size(); i++) {
            c -= p.get(i);
            if (c <= 0) {
                result.add(states.get(i));
                last = i;
                break;
            }
        }

        if (tokenCondition.test(result)) {
            return result;
        }

        Map<Integer, double[]> cache = new HashMap<>();

        while (true) {

            if (!cache.containsKey(last)) {
                RV ref = m.mapRow(last);
                double[] index = new double[ref.size()];
                for (int i = 0; i < ref.size(); i++) {
                    index[i] = ref.get(i);
                    if (i > 0) {
                        index[i] += index[i - 1];
                    }
                }
                cache.put(last, index);
            }

            double[] row = cache.get(last);
            c = RandomSource.nextDouble();

            int i = Arrays.binarySearch(row, c);
            if (i < 0) {
                i = -i - 1;
            }
            if (i == states.size())
                i--;
            result.add(states.get(i));
            last = i;

            if (tokenCondition.test(result)) {
                return result;
            }
        }
    }

    public String generateSentence(Predicate<List<String>> endCondition) {
        List<String> list = generateChain(endCondition);
        return adapter.restore(list);
    }

    @Override
    public String toSummary() {

        RandomSource.setSeed(1);

        StringBuilder sb = new StringBuilder();
        sb.append("MarkovChain model\n");
        sb.append("=================\n");
        sb.append("States: \n");
        sb.append("count: ").append(states.size()).append("\n");
        sb.append("values: \n");
        String buff = "";
        for (String state : states) {
            if (buff.length() + state.length() + 3 >= WS.getPrinter().textWidth()) {
                sb.append(buff).append("\n");
                buff = "";
            }
            buff = buff + "'" + state + "',";
        }
        if (!buff.isEmpty())
            sb.append(buff).append("\n");

        sb.append("Priors: \n");
        sb.append(p.toSummary());

        sb.append("Matrix: \n");
        sb.append(m.toSummary());

        return sb.toString();
    }
}
