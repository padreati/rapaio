package rapaio.data;


import rapaio.data.mapping.Mapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.DoubleStream;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class Numeric extends AbstractVector {

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	private static final int DEFAULT_CAPACITY = 10;
	private static final double[] EMPTY_DATA = {};

	private static final double missingValue = Double.NaN;
	private double[] data;
	private int rows;

	public Numeric() {
		super();
		this.data = EMPTY_DATA;
	}

	public Numeric(int rows) {
		this(rows, rows, Double.NaN);
	}

	public Numeric(int rows, int capacity) {
		this(rows, capacity, Double.NaN);
	}

	public Numeric(int rows, int capacity, double fill) {
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

	public Numeric(double[] values) {
		data = Arrays.copyOf(values, values.length);
		this.rows = values.length;
	}

	public Numeric(int[] values) {
		data = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			data[i] = values[i];
		}
		this.rows = values.length;
	}

	@Override
	public VectorType getType() {
		return VectorType.NUMERIC;
	}

	private void ensureCapacityInternal(int minCapacity) {
		if (data == EMPTY_DATA) {
			minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
		}
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

	public boolean addAll(Collection<? extends Double> c) {
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacityInternal(rows + numNew);  // Increments modCount
		System.arraycopy(a, 0, data, rows, numNew);
		rows += numNew;
		return numNew != 0;
	}

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

	private void rangeCheck(int index) {
		if (index >= rows || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + rows);
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
	public void addValue(double value) {
		ensureCapacityInternal(rows + 1);
		data[rows++] = value;
	}

	@Override
	public void addValue(int row, double value) {
		rangeCheck(row);
		ensureCapacityInternal(rows + 1);  // Increments modCount!!
		System.arraycopy(data, row, data, row + 1, rows - row);
		data[row] = value;
		rows++;
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
	public void addIndex(int value) {
		ensureCapacityInternal(rows + 1);
		data[rows++] = value;
	}

	@Override
	public void addIndex(int row, int value) {
		rangeCheck(row);
		ensureCapacityInternal(rows + 1);  // Increments modCount!!
		System.arraycopy(data, row, data, row + 1, rows - row);
		data[row] = value;
		rows++;
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
	public void addLabel(String value) {
		throw new RuntimeException("Operation not available for numeric vectors.");
	}

	@Override
	public void addLabel(int row, String value) {
		throw new RuntimeException("Operation not available for numeric vectors.");
	}

	@Override
	public String[] getDictionary() {
		throw new RuntimeException("Operation not available for numeric vectors.");
	}

	@Override
	public void setDictionary(String[] dict) {
		throw new RuntimeException("Operation not available for numeric vectors.");
	}

	@Override
	public boolean isMissing(int row) {
		return getValue(row) != getValue(row);
	}

	@Override
	public void setMissing(int row) {
		setValue(row, missingValue);
	}

	@Override
	public void addMissing() {
		addValue(missingValue);
	}

	@Override
	public void remove(int index) {
		rangeCheck(index);
		int numMoved = rows - index - 1;
		if (numMoved > 0)
			System.arraycopy(data, index + 1, data, index, numMoved);
	}

	@Override
	public void removeRange(int fromIndex, int toIndex) {
		int numMoved = rows - toIndex;
		System.arraycopy(data, toIndex, data, fromIndex, numMoved);
		rows -= (toIndex - fromIndex);
	}

	@Override
	public void clear() {
		rows = 0;
	}

	@Override
	public void trimToSize() {
		if (rows < data.length) {
			data = Arrays.copyOf(data, rows);
		}
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		int minExpand = (data != EMPTY_DATA) ? 0 : DEFAULT_CAPACITY;
		if (minCapacity > minExpand) {
			// overflow-conscious code
			if (minCapacity - data.length > 0)
				grow(minCapacity);
		}
	}

	@Override
	public String toString() {
		return "Numeric[" + getRowCount() + "]";
	}

	@Override
	public DoubleStream getDoubleStream() {
		return Arrays.stream(Arrays.copyOf(data, getRowCount()));
	}
}
