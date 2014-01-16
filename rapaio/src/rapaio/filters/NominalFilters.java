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
package rapaio.filters;

import rapaio.data.*;
import rapaio.data.Vector;

import java.util.*;

/**
 * Provides filter operations on numeric vectors.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class NominalFilters {

    private NominalFilters() {
    }

    /**
     * Set missing values for all nominal values included
     * in missing values array {@param missingValues}.
     *
     * @param vector        source vector
     * @param missingValues labels for missing values
     * @return original vector with missing value on matched positions
     */
    public static Vector fillMissingValues(Vector vector, String[] missingValues) {
        if (!vector.type().isNominal()) {
            throw new IllegalArgumentException("Vector is not isNominal.");
        }
        for (int i = 0; i < vector.rowCount(); i++) {
            String value = vector.label(i);
            for (String missingValue : missingValues) {
                if (value.equals(missingValue)) {
                    vector.setMissing(i);
                    break;
                }
            }
        }
        return vector;
    }

    /**
     * Split a frame into multiple frames, one for each label in
     * the term dictionary of the nominal value given as parameter.
     *
     * @param df           source frame
     * @param nominalIndex index fo the nominal column
     * @return an array of frames, one for each nominal label
     */
    public static Frame[] groupByNominal(final Frame df, final int nominalIndex) {
        if (!df.col(nominalIndex).type().isNominal()) {
            throw new IllegalArgumentException("Index does not specify a nominal attribute");
        }
        int len = df.col(nominalIndex).dictionary().length;
        ArrayList<Integer>[] mappings = new ArrayList[len];
        for (int i = 0; i < len; i++) {
            mappings[i] = new ArrayList<>();
        }
        for (int i = 0; i < df.rowCount(); i++) {
            mappings[df.index(i, nominalIndex)].add(df.rowId(i));
        }
        Frame[] frames = new Frame[len];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new MappedFrame(df.sourceFrame(), new Mapping(mappings[i]));
        }
        return frames;
    }

    /**
     * Consolidates all the common nominal columns from all frames given as parameter.
     *
     * @param source list of source frames
     * @return list of frames with common nominal columns consolidated
     */
    public static List<Frame> consolidate(List<Frame> source) {

        // learn reunion of labels for all columns
        HashMap<String, HashSet<String>> dicts = new HashMap<>();
        for (int i = 0; i < source.size(); i++) {
            for (Frame frame : source) {
                for (String colName : frame.colNames()) {
                    if (!frame.col(colName).type().isNominal()) {
                        continue;
                    }
                    if (!dicts.containsKey(colName)) {
                        dicts.put(colName, new HashSet<String>());
                    }
                    dicts.get(colName).addAll(Arrays.asList(frame.col(colName).dictionary()));
                }
            }
        }

        // rebuild each frame according with the new consolidated data
        List<Frame> dest = new ArrayList<>();
        for (Frame frame : source) {
            Vector[] vectors = new Vector[frame.colCount()];
            for (int i = 0; i < frame.colCount(); i++) {
                Vector v = frame.col(i);
                String colName = frame.colNames()[i];
                if (!v.type().isNominal()) {
                    vectors[i] = v;
                } else {
                    vectors[i] = new Nominal(v.rowCount(), dicts.get(colName));
                    for (int k = 0; k < vectors[i].rowCount(); k++) {
                        vectors[i].setLabel(k, v.label(k));
                    }
                }
            }
            dest.add(new SolidFrame(frame.rowCount(), vectors, frame.colNames()));
        }

        return dest;
    }

    public static List<Frame> combine(String name, List<Frame> frames, String... combined) {
        Set<String> dict = new HashSet<>();
        dict.add("");
        for (int i = 0; i < frames.size(); i++) {
            if (frames.get(i).isMappedFrame()) {
                throw new IllegalArgumentException("Not allowed mapped frames");
            }
        }
        for (int j = 0; j < combined.length; j++) {
            String[] vdict = frames.get(0).col(combined[j]).dictionary();
            Set<String> newdict = new HashSet<>();
            for (String term : dict) {
                for (int k = 0; k < vdict.length; k++) {
                    newdict.add(term + "." + vdict[k]);
                }
            }
            dict = newdict;
        }
        List<Frame> result = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            List<Vector> vectors = new ArrayList<>();
            for (int j = 0; j < frames.get(i).colCount(); j++) {
                vectors.add(frames.get(i).col(j));
            }
            Vector col = new Nominal(frames.get(i).rowCount(), dict);
            for (int j = 0; j < frames.get(i).rowCount(); j++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < combined.length; k++) {
                    sb.append(".").append(frames.get(i).label(j, frames.get(i).colIndex(combined[k])));
                }
                col.setLabel(j, sb.toString());
            }
            vectors.add(col);
            result.add(new SolidFrame(frames.get(i).rowCount(), vectors, frames.get(i).colNames()));
        }
        return result;
    }

}
