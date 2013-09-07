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

import rapaio.data.Vector;

import java.util.Comparator;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 8/26/13
 * Time: 3:29 PM
 */
public class FilterRename implements Filter {

    public Vector rename(final Vector vector, final String name) {
        return new Vector() {
            @Override
            public boolean isNumeric() {
                return vector.isNumeric();
            }

            @Override
            public boolean isNominal() {
                return vector.isNominal();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getRowCount() {
                return vector.getRowCount();
            }

            @Override
            public int getRowId(int row) {
                return vector.getRowId(row);
            }

            @Override
            public double getValue(int row) {
                return vector.getValue(row);
            }

            @Override
            public void setValue(int row, double value) {
                vector.setValue(row, value);
            }

            @Override
            public int getIndex(int row) {
                return vector.getIndex(row);
            }

            @Override
            public void setIndex(int row, int value) {
                vector.setIndex(row, value);
            }

            @Override
            public String getLabel(int row) {
                return vector.getLabel(row);
            }

            @Override
            public void setLabel(int row, String value) {
                vector.setLabel(row, value);
            }

            @Override
            public String[] dictionary() {
                return vector.dictionary();
            }

            @Override
            public boolean isMissing(int row) {
                return vector.isMissing(row);
            }

            @Override
            public void setMissing(int row) {
                vector.setMissing(row);
            }

            @Override
            public Comparator<Integer> getComparator(boolean asc) {
                return vector.getComparator(asc);
            }
        };
    }
}
