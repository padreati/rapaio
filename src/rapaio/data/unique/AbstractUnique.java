/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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
 *
 */

package rapaio.data.unique;

import rapaio.data.Mapping;
import rapaio.data.Unique;
import rapaio.data.VarInt;
import rapaio.printer.format.Format;
import rapaio.printer.format.TextTable;
import rapaio.util.collection.IntArrays;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/23/18.
 */
public abstract class AbstractUnique implements Unique {

    protected final boolean sorted;
    protected HashMap<Integer, Mapping> rowLists;
    protected VarInt countSortedIds;
    protected VarInt valueSortedIds;
    protected int[] idsByRow;

    public AbstractUnique(boolean sorted) {
        this.sorted = sorted;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    protected void updateIdsByRow(int len) {
        idsByRow = new int[len];
        for (Map.Entry<Integer, Mapping> e : rowLists.entrySet()) {
            for (int row : e.getValue()) {
                idsByRow[row] = e.getKey();
            }
        }
    }

    @Override
    public VarInt countSortedIds() {
        if (countSortedIds == null) {
            int[] counts = new int[uniqueCount()];
            int[] ids2 = new int[uniqueCount()];
            for (int i = 0; i < uniqueCount(); i++) {
                counts[i] = rowLists.get(i).size();
                ids2[i] = i;
            }
            IntArrays.quickSort(ids2, 0, uniqueCount(), (i, j) -> Integer.compare(counts[i], counts[j]));
            countSortedIds = VarInt.wrap(ids2);
        }
        return countSortedIds;
    }

    @Override
    public int idByRow(int row) {
        return idsByRow[row];
    }

    protected abstract String stringClass();

    protected abstract String stringUniqueValue(int i);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stringClass());
        sb.append("{count=").append(uniqueCount()).append(", ");
        sb.append("values=[");
        if (uniqueCount() > 10) {
            for (int i = 0; i < 10; i++) {
                sb.append(stringUniqueValue(i)).append(":").append(rowList(i).size()).append(",");
            }
            sb.append("..]}");
        } else {
            for (int i = 0; i < uniqueCount(); i++) {
                sb.append(stringUniqueValue(i)).append(":").append(rowList(i).size());
                if (i != uniqueCount() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");
        }
        return sb.toString();
    }

    @Override
    public String toContent() {
        int max = uniqueCount();
        if (max > 40) {
            max = 40;
        }
        double total = rowLists.values().stream().mapToDouble(Mapping::size).sum();
        if (uniqueCount() > max) {
            TextTable tt = TextTable.empty(max + 1, 3, 1, 0);
            tt.textCenter(0, 0, "Value");
            tt.textCenter(0, 1, "Count");
            tt.textCenter(0, 2, "Percentage");

            for (int i = 0; i < 30; i++) {
                tt.textRight(i + 1, 0, stringUniqueValue(i));
                tt.textRight(i + 1, 1, Integer.toString(rowList(i).size()));
                tt.textRight(i + 1, 2, Format.floatShort(rowList(i).size() / total));
            }
            tt.textCenter(30 + 1, 0, "...");
            tt.textCenter(30 + 1, 1, "...");
            for (int i = 31; i < 40; i++) {
                tt.textRight(i + 1, 0, stringUniqueValue(uniqueCount() - 40 + i));
                tt.textRight(i + 1, 1, Integer.toString(rowList(uniqueCount() - 40 + i).size()));
                tt.textRight(i + 1, 2, Format.floatShort(rowList(uniqueCount() - 40 + i).size() / total));
            }
            return tt.getDynamicText();
        }
        return toFullContent();
    }

    @Override
    public String toFullContent() {
        TextTable tt = TextTable.empty(uniqueCount() + 1, 3, 1, 0);
        tt.textCenter(0, 0, "Value");
        tt.textCenter(0, 1, "Count");
        tt.textCenter(0, 2, "Percentage");

        double total = rowLists.values().stream().mapToDouble(Mapping::size).sum();
        for (int i = 0; i < uniqueCount(); i++) {
            tt.textRight(i + 1, 0, stringUniqueValue(i));
            tt.textRight(i + 1, 1, Integer.toString(rowList(i).size()));
            tt.textRight(i + 1, 2, Format.floatShort(rowList(i).size() / total));
        }
        return tt.getDynamicText();
    }

    @Override
    public String toSummary() {
        return toString();
    }
}
