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

package rapaio.data;

/**
 * Base class for a vector which enforces to read-only name given at construction time.
 * <p/>
 * It also provides behavior for the utility access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

    private final String name;

    public AbstractFrame(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getValue(int row, int col) {
        return getCol(col).getValue(row);
    }

    @Override
    public void setValue(int row, int col, double value) {
        getCol(col).setValue(row, value);
    }

    @Override
    public int getIndex(int row, int col) {
        return getCol(col).getIndex(row);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        getCol(col).setIndex(row, value);
    }

    @Override
    public String getLabel(int row, int col) {
        return getCol(col).getLabel(row);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        getCol(col).setLabel(row, value);
    }
}
