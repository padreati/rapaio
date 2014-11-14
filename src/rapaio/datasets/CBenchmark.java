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

package rapaio.datasets;

import rapaio.core.sample.Sampling;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.datasets.UCI.UCI;
import rapaio.io.ArffPersistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class which provides data sets for classification task.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class CBenchmark {

    private List<CTask> tasks = new ArrayList<>();

    public CBenchmark() throws IOException {

        tasks.add(new CTask() {

            {
                name = "iris";
                targetName = "class";
                full = new ArffPersistence().read(UCI.class.getResourceAsStream("iris.arff"));
                reSample(0.7, false);
            }

            @Override
            public boolean reSample(double p, boolean replacement) {
                int[] rows = replacement
                        ? new Sampling().sampleWR((int) (full.rowCount() * p), full.rowCount())
                        : new Sampling().sampleWOR((int) (full.rowCount() * p), full.rowCount());
                train = MappedFrame.newByRow(full, rows);
                Set<Integer> used = Arrays.stream(rows).mapToObj(row -> row).collect(Collectors.toSet());
                Mapping diff = Mapping.newCopyOf(IntStream.range(0, full.rowCount()).filter(row -> !used.contains(row)).toArray());
                test = MappedFrame.newByRow(full, diff);
                return true;
            }
        });
    }

    public List<CTask> getDefaultTasks() {
        return new ArrayList<>(tasks);
    }

    public CTask getTask(String name) {
        return tasks.stream().filter(t -> t.getName().equals(name)).findFirst().get();
    }
}
