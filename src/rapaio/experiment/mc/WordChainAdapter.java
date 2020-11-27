/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/2/15.
 */
public class WordChainAdapter implements ChainAdapter {

    final int n = 2;

    @Override
    public List<String> tokenize(String rawChain) {

        List<String> words = new ArrayList<>();
        BreakIterator breakIterator = BreakIterator.getWordInstance();
        breakIterator.setText(rawChain);
        int lastIndex = breakIterator.first();
        while (BreakIterator.DONE != lastIndex) {
            int firstIndex = lastIndex;
            lastIndex = breakIterator.next();
            if (lastIndex != BreakIterator.DONE &&
                    (Character.isLetterOrDigit(rawChain.charAt(firstIndex))
                            || Character.isJavaIdentifierPart(rawChain.charAt(firstIndex)))) {
                words.add(rawChain.substring(firstIndex, lastIndex));
            }
        }
        words.add(".");

        List<String> result = new ArrayList<>();
        for (int i = n; i < words.size(); i++) {
            result.add(String.join(" ", words.subList(i - n, i)));
        }
        return result;
    }

    @Override
    public String restore(List<String> chain) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chain.size(); i++) {
            if (i == 0)
                sb.append(chain.get(i));
            else {
                String[] tok = chain.get(i).split(" ", -1);
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(tok[tok.length - 1]);
            }
        }
        return sb.toString();
    }
}
