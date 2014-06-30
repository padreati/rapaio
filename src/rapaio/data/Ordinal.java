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

package rapaio.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class Ordinal extends FactorBase {
    public static Ordinal newEmpty() {
        return new Ordinal();
    }

    public static Ordinal newEmpty(int rows, String... dict) {
        return Ordinal.newEmpty(rows, Arrays.asList(dict));
    }

    public static Ordinal newEmpty(int rows, Collection<String> dict) {
        Ordinal nominal = new Ordinal();
        for (String next : dict) {
            if (nominal.dict.contains(next)) continue;
            nominal.dict.add(next);
            nominal.reverse.put(next, nominal.reverse.size());
        }
        nominal.data = new int[rows];
        nominal.rows = rows;
        return nominal;
    }

    protected Ordinal() {
        super();
        // set the missing value
        this.reverse = new HashMap<>();
        this.reverse.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new int[0];
        rows = 0;
    }

    @Override
    public VarType type() {
        return VarType.ORDINAL;
    }

    @Override
    public Ordinal solidCopy() {
        Ordinal copy = Ordinal.newEmpty(rowCount(), dictionary());
        for (int i = 0; i < rowCount(); i++) {
            copy.setLabel(i, label(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Ordinal[" + rowCount() + "]";
    }
}