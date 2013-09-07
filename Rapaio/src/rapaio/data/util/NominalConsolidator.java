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

package rapaio.data.util;

import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;

import java.util.*;

/**
 * Utility class which consolidates all the common nominal vectors.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NominalConsolidator {

    public static List<Frame> consolidate(List<Frame> source) {

        // build reunion of labels for all columns
        HashMap<String, HashSet<String>> dicts = new HashMap<>();
        for (int i = 0; i < source.size(); i++) {
            for (Frame frame : source) {
                for (String colName : frame.getColNames()) {
                    if (!frame.getCol(colName).isNominal()) {
                        continue;
                    }
                    if (!dicts.containsKey(colName)) {
                        dicts.put(colName, new HashSet<String>());
                    }
                    dicts.get(colName).addAll(Arrays.asList(frame.getCol(colName).dictionary()));
                }
            }
        }

        // rebuild each frame according with the new consolidated data
        List<Frame> dest = new ArrayList<>();
        for (Frame frame : source) {
            Vector[] vectors = new Vector[frame.getColCount()];
            for (int i = 0; i < frame.getColCount(); i++) {
                Vector v = frame.getCol(i);
                String colName = frame.getColNames()[i];
                if (!v.isNominal()) {
                    vectors[i] = v;
                } else {
                    Vector newv = new NominalVector(colName, v.getRowCount(), dicts.get(colName));
                    for (int k = 0; k < newv.getRowCount(); k++) {
                        newv.setLabel(k, v.getLabel(k));
                    }
                    vectors[i] = newv;
                }
            }
            dest.add(new SolidFrame(frame.getName(), frame.getRowCount(), vectors));
        }

        return dest;
    }
}
