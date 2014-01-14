package rapaio.data;


import java.util.Arrays;
import java.util.Collection;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NumVector extends AbstractVector {

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final int DEFAULT_CAPACITY = 10;
    private static final double[] EMPTY_DATA = {};

    private static final double missingValue = Double.NaN;
    private transient double[] data;
    private int rows;

    public NumVector() {
        super();
        this.data = EMPTY_DATA;
    }

    public NumVector(int rows) {
        this(rows, rows, Double.NaN);
    }

    public NumVector(int rows, int capacity) {
        this(rows, capacity, Double.NaN);
    }

    public NumVector(int rows, int capacity, double fill) {
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
        this.data = new double[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    public NumVector(double[] values) {
        data = Arrays.copyOf(values, values.length);
        this.rows = values.length;
    }

    public NumVector(int[] values) {
        data = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            data[i] = values[i];
        }
        this.rows = values.length;
    }

    /**
     * Trims the capacity to be the list's current size.
     * An application can use this operation to minimize the storage.
     */
    public void trimToSize() {
        if (rows < data.length) {
            data = Arrays.copyOf(data, rows);
        }
    }

    /**
     * Increases the capacity of this instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (data != EMPTY_DATA)
                // any size if real element table
                ? 0
                // larger than default for empty table. It's already supposed to be
                // at default size.
                : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (data == EMPTY_DATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - data.length > 0)
            grow(minCapacity);
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
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
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    // Positional Access Operations

    /**
     * Appends the specified element to the end of this list.
     *
     * @param x element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(double x) {
        ensureCapacityInternal(rows + 1);  // Increments modCount!!
        data[rows++] = x;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, double element) {
        rangeCheck(index);

        ensureCapacityInternal(rows + 1);  // Increments modCount!!
        System.arraycopy(data, index, data, index + 1,
                rows - index);
        data[index] = element;
        rows++;
    }

    /**
     * Removes the element at the specified position in this vector.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public double remove(int index) {
        rangeCheck(index);
        double oldValue = data[index];

        int numMoved = rows - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
        return oldValue;
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        rows = 0;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends Double> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(rows + numNew);  // Increments modCount
        System.arraycopy(a, 0, data, rows, numNew);
        rows += numNew;
        return numNew != 0;
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c     collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException      if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends Double> c) {
        rangeCheck(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(rows + numNew);  // Increments modCount

        int numMoved = rows - index;
        if (numMoved > 0)
            System.arraycopy(data, index, data, index + numNew,
                    numMoved);

        System.arraycopy(a, 0, data, index, numNew);
        rows += numNew;
        return numNew != 0;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *                                   {@code toIndex} is out of range
     *                                   ({@code fromIndex < 0 ||
     *                                   fromIndex >= size() ||
     *                                   toIndex > size() ||
     *                                   toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        int numMoved = rows - toIndex;
        System.arraycopy(data, toIndex, data, fromIndex,
                numMoved);

        // clear to let GC do its work
        int newSize = rows - (toIndex - fromIndex);
        rows = newSize;
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(int index) {
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
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
    public double getValue(int row) {
        rangeCheck(row);
        return data[row];
    }

    @Override
    public void setValue(int row, double value) {
        rangeCheck(row);
        data[row] = value;
    }

    @Override
    public int getIndex(int row) {
        return (int) Math.rint(getValue(row));
    }

    @Override
    public void setIndex(int row, int value) {
        setValue(row, value);
    }

    @Override
    public String getLabel(int row) {
        return "";
    }

    @Override
    public void setLabel(int row, String value) {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public String[] getDictionary() {
        return new String[0];
    }

    @Override
    public boolean isMissing(int row) {
        return getValue(row) != getValue(row);
    }

    @Override
    public void setMissing(int row) {
        setValue(row, missingValue);
    }
}
