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

package rapaio.core;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.filters.BaseFilters;
import rapaio.io.CsvPersistence;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class CoreStatTestUtil {

    private Frame df;

    public CoreStatTestUtil() throws IOException {
        CsvPersistence p = new CsvPersistence();
        p.setHasHeader(false);
        df = p.read("core_stat", CoreStatTestUtil.class.getResourceAsStream("core_stat.csv"));
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = BaseFilters.toNumeric(df.getCol(i).getName(), df.getCol(i));
        }
        df = new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public Frame getDataFrame() {
        return df;
    }
}