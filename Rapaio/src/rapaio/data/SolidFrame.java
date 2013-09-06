/*
 * 
 */
package rapaio.data;

import java.util.HashMap;

/**
 * @author Aurelian Tutuianu
 */
public class SolidFrame extends AbstractFrame {

    private final int rows;
    private final Vector[] vectors;
    private final HashMap<String, Integer> mapping;
    private final String[] names;

    public SolidFrame(String name, int rows, Vector[] vectors) {
        super(name);
        this.rows = rows;
        this.vectors = new Vector[vectors.length];
        this.mapping = new HashMap<>();
        this.names = new String[vectors.length];

        for (int i = 0; i < vectors.length; i++) {
            this.vectors[i] = vectors[i];
            this.mapping.put(vectors[i].getName(), i);
            names[i] = vectors[i].getName();
        }
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColCount() {
        return vectors.length;
    }

    @Override
    public int rowId(int row) {
        return row;
    }

    @Override
    public String[] getColNames() {
        return names;
    }

    @Override
    public int getColIndex(String name) {
        return mapping.get(name);
    }

    @Override
    public Vector getCol(int col) {
        if (col >= 0 && col < vectors.length) {
            return vectors[col];
        }
        throw new IllegalArgumentException("Invalid column index");
    }

    @Override
    public Vector getCol(String name) {
        return getCol(getColIndex(name));
    }
}
