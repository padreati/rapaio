/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.filters;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;

import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterGroupByNominal {

    public Frame[] groupByNominal(Frame df, int nominalIndex) {
        if (!df.getCol(nominalIndex).isNominal()) {
            throw new IllegalArgumentException("Index does not specify a isNominal attribute");
        }
        int len = df.getCol(nominalIndex).dictionary().length;
        ArrayList<Integer>[] mappings = new ArrayList[len];
        for (int i = 0; i < len; i++) {
            mappings[i] = new ArrayList<>();
        }
        for (int i = 0; i < df.getRowCount(); i++) {
            mappings[df.getCol(nominalIndex).getIndex(i)].add(i);
        }
        Frame[] frames = new Frame[len];
        for (int i = 0; i < frames.length; i++) {
            if (mappings[i].isEmpty()) {
                continue;
            }
            frames[i] = new MappedFrame(df, mappings[i]);
        }
        return frames;
    }
}
