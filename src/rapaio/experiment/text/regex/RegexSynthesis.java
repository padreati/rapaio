/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.text.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;

public class RegexSynthesis {


    public static void main(String[] args) {
        Frame df = Datasets.loadPhoneData65k();

        var names = df.rvar(0);
        var numbers = df.rvar(1);

        List<ScriptSnippet> scriptSnippets = numbers.stream().map(s -> new ScriptSnippet(s.getLabel())).collect(Collectors.toList());
        for(ScriptSnippet ss : scriptSnippets) {
            System.out.print(ss.text + "  -> [");
            for (int i = 0; i < ss.getScripts().size(); i++) {
                System.out.print(ss.scripts.get(i) + ":" + ss.counts.get(i) + ",");
            }
            System.out.println("]");
        }
    }

    private static void inspect(int codePoint) {

        CodePoint cp = new CodePoint(codePoint);

        System.out.printf("%06d %s (%s), type: %s, %s - %s\n",
                cp.numericValue(),
                cp,
                cp.name(),
                cp.typeName(),
                cp.scriptName(),
                cp.blockName());
    }

    public static abstract class Snippet {

        protected final String text;
        protected final CodePoint[] data;

        public Snippet(String text) {
            this.text = text;
            this.data = text.codePoints().mapToObj(CodePoint::new).toArray(CodePoint[]::new);
        }
    }

    public static class ScriptSnippet extends Snippet implements Comparable<ScriptSnippet> {

        private final List<String> scripts = new ArrayList<>();
        private final List<Integer> counts = new ArrayList<>();

        public ScriptSnippet(String text) {
            super(text);
            if (data.length != 0) {
                int pos = 1;
                String lastScript = data[0].typeName();
                int lastCount = 1;
                while (pos <= data.length) {
                    if (pos == data.length) {
                        scripts.add(lastScript);
                        counts.add(lastCount);
                        break;
                    }
                    String script = data[pos].typeName();
                    if (script.equals(lastScript)) {
                        lastCount++;
                        pos++;
                    } else {
                        scripts.add(lastScript);
                        counts.add(lastCount);
                        lastScript = script;
                        lastCount = 1;
                        pos++;
                    }
                }
            }
        }

        public List<String> getScripts() {
            return scripts;
        }

        public List<Integer> getCounts() {
            return counts;
        }

        @Override
        public int compareTo(ScriptSnippet o) {
            return 0;
        }
    }
}
