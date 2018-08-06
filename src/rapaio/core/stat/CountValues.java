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

package rapaio.core.stat;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.TreeSet;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/18.
 */
public class CountValues implements Printable {

    public static final int DEFAULT_TOP = 30;

    public static CountValues fromLabels(Var var) {
        return new CountValues(var, DEFAULT_TOP);
    }

    public static CountValues fromLabels(Var var, int top) {
        return new CountValues(var, top);
    }

    private final String varName;
    private final int top;
    private final Int2ObjectSortedMap<TreeSet<String>> reverse = new Int2ObjectAVLTreeMap<>((o1, o2) -> -Integer.compare(o1, o2));

    private CountValues(Var var, int top) {
        this.varName = var.name();
        this.top = top;

        Object2IntOpenHashMap<String> hash = new Object2IntOpenHashMap<>(var.rowCount() / 2);
        for (int i = 0; i < var.rowCount(); i++) {
            String label = var.getLabel(i);
            if (!hash.containsKey(label)) {
                hash.put(label, 0);
            }
            hash.put(label, hash.getInt(label) + 1);
        }
        for(String key : hash.keySet()) {
            int count = hash.getInt(key);
            if(!reverse.containsKey(count)) {
                reverse.put(count, new TreeSet<>());
            }
            reverse.get(count).add(key);
        }
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("count values var[").append(varName).append("]:\n");

        int pos = 0;
        for(int key : reverse.keySet()) {
            for(String label : reverse.get(key)) {
                if(pos>=top) {
                    break;
                }
                sb.append(String.format("%40s : %d%n", label, key));
                pos++;
            }
        }

        return sb.toString();
    }
}
