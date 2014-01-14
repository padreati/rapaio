package rapaio.data;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class IdxVector extends AbstractVector {

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final int DEFAULT_CAPACITY = 10;
    private static final int[] EMPTY_DATA = {};

    private static final int MISSING_VALUE = Integer.MIN_VALUE;
    private transient int[] data;
    private int rows;

    public IdxVector(int rows, int capacity, int fill) {
        super();
        if (capacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        if (rows > capacity) {
            throw new IllegalArgumentException(
                    "Illegal row count" + rows + " less than capacity:" + capacity);
        }
        this.data = new int[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    public IdxVector(int[] values) {
        data = Arrays.copyOf(values, values.length);
        this.rows = values.length;
    }

    public void trimToSize() {
        if (rows < data.length) {
            data = Arrays.copyOf(data, rows);
        }
    }

    public void ensureCapacity(int minCapacity) {
        int minExpand = (data != EMPTY_DATA) ? 0 : DEFAULT_CAPACITY;
        if (minCapacity > minExpand && minCapacity - data.length > 0)
            grow(minCapacity);
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (data == EMPTY_DATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        if (minCapacity - data.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    // Positional Access Operations

    public void add(int x) {
        ensureCapacityInternal(rows + 1);
        data[rows++] = x;
    }

    public void add(int index, int element) {
        rangeCheck(index);

        ensureCapacityInternal(rows + 1);
        System.arraycopy(data, index, data, index + 1, rows - index);
        data[index] = element;
        rows++;
    }

    public double remove(int index) {
        rangeCheck(index);
        double oldValue = data[index];
        int numMoved = rows - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
        return oldValue;
    }

    public void clear() {
        rows = 0;
    }

    public boolean addAll(Collection<? extends Double> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(rows + numNew);
        System.arraycopy(a, 0, data, rows, numNew);
        rows += numNew;
        return numNew != 0;
    }

    public boolean addAll(int index, Collection<? extends Double> c) {
        rangeCheck(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(rows + numNew);

        int numMoved = rows - index;
        if (numMoved > 0)
            System.arraycopy(data, index, data, index + numNew, numMoved);

        System.arraycopy(a, 0, data, index, numNew);
        rows += numNew;
        return numNew != 0;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        int numMoved = rows - toIndex;
        System.arraycopy(data, toIndex, data, fromIndex,
                numMoved);

        // clear to let GC do its work
        int newSize = rows - (toIndex - fromIndex);
        rows = newSize;
    }

    private void rangeCheck(int index) {
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + rows;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean isNominal() {
        return false;
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
        return rows;
    }

    @Override
    public int getRowId(int row) {
        return row;
    }

    @Override
    public int getIndex(int row) {
        return data[row];
    }

    @Override
    public void setIndex(int row, int value) {
        data[row] = value;
    }

    @Override
    public double getValue(int row) {
        return getIndex(row);
    }

    @Override
    public void setValue(int row, double value) {
        setIndex(row, (int) Math.rint(value));
    }

    @Override
    public String getLabel(int row) {
        return "";
    }

    @Override
    public void setLabel(int row, String value) {
        throw new RuntimeException("Operation not available for index vectors.");
    }

    @Override
    public String[] getDictionary() {
        return new String[0];
    }

    @Override
    public boolean isMissing(int row) {
        return getIndex(row) == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, MISSING_VALUE);
    }
}
