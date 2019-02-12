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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.printer.format.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/23/18.
 */
public abstract class AbstractUnique implements Unique {

    protected final boolean sorted;
    protected Int2ObjectOpenHashMap<IntList> rowLists;
    protected IntArrayList countSortedIds;
    protected IntArrayList valueSortedIds;
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
        for (Int2ObjectOpenHashMap.Entry<IntList> e : rowLists.int2ObjectEntrySet()) {
            for (int row : e.getValue()) {
                idsByRow[row] = e.getIntKey();
            }
        }
    }

    @Override
    public IntList countSortedIds() {
        if (countSortedIds == null) {
            int[] counts = new int[uniqueCount()];
            int[] ids2 = new int[uniqueCount()];
            for (int i = 0; i < uniqueCount(); i++) {
                counts[i] = rowLists.get(i).size();
                ids2[i] = i;
            }
            IntArrays.quickSort(ids2, (i, j) -> Integer.compare(counts[i], counts[j]));
            countSortedIds = new IntArrayList(ids2);
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
    public String content() {
        int max = uniqueCount();
        if (max > 40) {
            max = 40;
        }
        if (uniqueCount() > max) {
            TextTable tt = TextTable.empty(max + 1, 2, 1, 0);
            tt.textCenter(0, 0, "Value");
            tt.textCenter(0, 1, "Count");

            for (int i = 0; i < 30; i++) {
                tt.textRight(i + 1, 0, stringUniqueValue(i));
                tt.textRight(i + 1, 1, Integer.toString(rowList(i).size()));
            }
            tt.textCenter(30 + 1, 0, "...");
            tt.textCenter(30 + 1, 1, "...");
            for (int i = 31; i < 40; i++) {
                tt.textRight(i+1, 0, stringUniqueValue(uniqueCount() - 40 + i));
                tt.textRight(i+1, 1, Integer.toString(rowList(uniqueCount() -40+ i).size()));
            }
            return tt.getDefaultText();
        }
        return fullContent();
    }

    @Override
    public String fullContent() {
        TextTable tt = TextTable.empty(uniqueCount() + 1, 2, 1, 0);
        tt.textCenter(0, 0, "Value");
        tt.textCenter(0, 1, "Count");

        for (int i = 0; i < uniqueCount(); i++) {
            tt.textRight(i + 1, 0, stringUniqueValue(i));
            tt.textRight(i + 1, 1, Integer.toString(rowList(i).size()));
        }
        return tt.getDefaultText();
    }

    @Override
    public String summary() {
        return toString();
    }
}
