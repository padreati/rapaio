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

package rapaio.data;

import rapaio.data.stream.FSpot;
import rapaio.data.stream.FSpots;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

    @Override
    public double value(int row, int col) {
        return col(col).value(row);
    }

    @Override
    public double value(int row, String colName) {
        return col(colName).value(row);
    }

    @Override
    public void setValue(int row, int col, double value) {
        col(col).setValue(row, value);
    }

    @Override
    public void setValue(int row, String colName, double value) {
        col(colName).setValue(row, value);
    }

    @Override
    public int index(int row, int col) {
        return col(col).index(row);
    }

    @Override
    public int index(int row, String colName) {
        return col(colName).index(row);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        col(col).setIndex(row, value);
    }

    @Override
    public void setIndex(int row, String colName, int value) {
        col(colName).setIndex(row, value);
    }

    @Override
    public String label(int row, int col) {
        return col(col).label(row);
    }

    @Override
    public String label(int row, String colName) {
        return col(colName).label(row);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        col(col).setLabel(row, value);
    }

    @Override
    public void setLabel(int row, String colName, String value) {
        col(colName).setLabel(row, value);
    }

    @Override
    public boolean missing(int row, int col) {
        return col(col).missing(row);
    }

    @Override
    public boolean missing(int row, String colName) {
        return col(colName).missing(row);
    }

    @Override
    public boolean missing(int row) {
        for (String colName : colNames()) {
            if (col(colName).missing(row)) return true;
        }
        return false;
    }

    @Override
    public void setMissing(int row, int col) {
        col(col).setMissing(row);
    }

    @Override
    public void setMissing(int row, String colName) {
        col(colName).setMissing(row);
    }

    private List<FSpot> streamList;

    public FSpots stream() {
        if (streamList == null || streamList.size() != rowCount()) {
            streamList = new ArrayList<>();
            for (int i = 0; i < rowCount(); i++) {
                streamList.add(new FSpot(this, i));
            }
        }
        return new FSpots(streamList.stream());
    }
}
