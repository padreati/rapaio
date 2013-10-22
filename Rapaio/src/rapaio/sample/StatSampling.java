/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.sample;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.filters.NominalFilters;
import static rapaio.filters.RowFilters.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class StatSampling {

    public static List<Frame> randomSample(Frame frame, int splits) {
        int[] rowCounts = new int[splits - 1];
        for (int i = 0; i < splits - 1; i++) {
            rowCounts[i] = frame.getRowCount() / splits;
        }
        return randomSample(frame, rowCounts);
    }

    public static List<Frame> randomSample(Frame frame, int[] rowCounts) {
        int total = 0;
        for (int i = 0; i < rowCounts.length; i++) {
            total += rowCounts[i];
        }
        if (total > frame.getRowCount()) {
            throw new IllegalArgumentException("total counts greater than available number of rows");
        }
        List<Frame> result = new ArrayList<>();
        Frame shuffle = shuffle(frame);
        int len = 0;
        for (int i = 0; i < rowCounts.length; i++) {
            List<Integer> mapping = new ArrayList<>();
            for (int j = 0; j < rowCounts[i]; j++) {
                mapping.add(shuffle.getRowId(len + j));
            }
            result.add(new MappedFrame(shuffle.getSourceFrame().getName(), shuffle.getSourceFrame(), new Mapping(mapping)));
            len += rowCounts[i];
        }
        if (len < shuffle.getRowCount()) {
            List<Integer> mapping = new ArrayList<>();
            for (int j = len; j < shuffle.getRowCount(); j++) {
                mapping.add(shuffle.getRowId(j));
            }
            result.add(new MappedFrame(shuffle.getSourceFrame().getName(), shuffle.getSourceFrame(), new Mapping(mapping)));
        }
        return result;
    }

    public static Frame randomBootstrap(Frame frame) {
        return randomBootstrap(frame, frame.getRowCount());
    }

    public static Frame randomBootstrap(Frame frame, int size) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int next = RandomSource.nextInt(frame.getRowCount());
            mapping.add(frame.getRowId(next));
        }
        return new MappedFrame(frame.getSourceFrame(), new Mapping(mapping));
    }

    public static Frame stratifiedBootstrap(Frame frame, String... strataCols) {
        List<Frame> frames = new ArrayList<>();
        frames.add(frame);
        for (int i = 0; i < strataCols.length; i++) {
            String col = strataCols[i];
            List<Frame> split = new ArrayList<>();
            for (Frame f : frames) {
                Frame[] groups = NominalFilters.groupByNominal(f, f.getColIndex(col));
                for (Frame group : groups) {
                    if (group != null && group.getRowCount() > 0) {
                        split.add(group);
                    }
                }
            }
            frames = split;
        }
        List<Integer> mapping = new ArrayList<>();
        for (Frame f : frames) {
            for (int i = 0; i < f.getRowCount(); i++) {
                mapping.add(f.getRowId(RandomSource.nextInt(f.getRowCount())));
            }
        }
        return new MappedFrame(frame.getSourceFrame(), new Mapping(mapping));
    }

}
