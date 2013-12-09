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

import java.util.*;

/**
 * Nominal vector contains values for nominal or categorical observations.
 * <p/>
 * The domain of the definition is called dictionary and is
 * given at construction time.
 * <p/>
 * This vector accepts two value representation: as labels and as indexes.
 * <p/>
 * Label representation is the natural representation since in experiments
 * the nominal vectors are given as string values.
 * <p/>
 * The index representation is build based on the canonical form of the
 * term dictionary and is used often for performance reasons instead of
 * label representation, where the actual label value does not matter.
 *
 * @author Aurelian Tutuianu
 */
public class NominalVector extends AbstractVector {

    private static final String missingValue = "?";
    private static final int missingIndex = 0;
    private final String[] terms;
    private final int[] indexes;

    public NominalVector(int size, String[] dict) {
        this(size, Arrays.asList(dict));
    }

    public NominalVector(int size, Collection<String> dict) {
        TreeSet<String> copy = new TreeSet<>(dict);
        copy.remove(missingValue);
        terms = new String[copy.size() + 1];
        Iterator<String> it = copy.iterator();
        int pos = 0;
        terms[pos++] = missingValue;
        while (it.hasNext()) {
            terms[pos++] = it.next();
        }
        this.indexes = new int[size];
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isNominal() {
        return true;
    }

    @Override
    public boolean isMappedVector() {
        return false;
    }

    @Override
    public Vector getSourceVector() {
        return this;
    }

    @Override
    public Mapping getMapping() {
        return null;
    }

    @Override
    public int getRowCount() {
        return indexes.length;
    }

    @Override
    public int getRowId(int row) {
        return row;
    }

    @Override
    public int getIndex(int row) {
        return indexes[row];
    }

    @Override
    public void setIndex(int row, int value) {
        indexes[row] = value;
    }

    @Override
    public double getValue(int row) {
        return indexes[row];
    }

    @Override
    public void setValue(int row, double value) {
        setIndex(row, (int) Math.rint(value));
    }

    @Override
    public String getLabel(int row) {
        return terms[indexes[row]];
    }

    @Override
    public void setLabel(int row, String value) {
        if (value.equals(missingValue)) {
            indexes[row] = missingIndex;
            return;
        }
        int idx = Arrays.binarySearch(terms, 1, terms.length, value);
        if (idx < 0) {
            throw new IllegalArgumentException("Can't set a getLabel that is not defined.");
        }
        indexes[row] = idx;
    }

    @Override
    public String[] getDictionary() {
        return Arrays.copyOf(terms, terms.length);
    }

    @Override
    public boolean isMissing(int row) {
        return missingIndex == getIndex(row);
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, missingIndex);
    }
}
