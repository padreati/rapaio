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

package rapaio.ml.colselect;

import rapaio.core.ColRange;
import rapaio.data.Frame;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class DefaultColSelector implements ColSelector {

    private String[] selection;

    public DefaultColSelector(Frame df, ColRange except) {
        String[] all = df.colNames();
        List<Integer> ex = except.parseColumnIndexes(df);
        selection = new String[all.length - ex.size()];
        int pos = 0;
        int spos = 0;
        for (int i = 0; i < all.length; i++) {
            if (pos < ex.size() && i == ex.get(pos)) {
                pos++;
                continue;
            }
            selection[spos++] = all[i];
        }
    }

    @Override
    public String[] nextColNames() {
        return selection;
    }
}
