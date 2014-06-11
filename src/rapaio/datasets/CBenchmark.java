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

import rapaio.core.RandomSource;
import rapaio.core.sample.StatSampling;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.UCI.UCI;
import rapaio.io.ArffPersistence;

import java.io.IOException;
import java.util.*;

/**
 * Utility class which provides data sets for classification task.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
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
                if (replacement)
                    train = StatSampling.randomBootstrap(full, (int) (full.rowCount() * p));
                else
                    train = StatSampling.randomSample(full, new int[]{(int) (full.rowCount() * p)}).get(0);
                test = BaseFilters.delta(full, train);
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
