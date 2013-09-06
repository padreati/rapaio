package rapaio.filters;

import rapaio.data.Vector;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterMissingNominal {

    public Vector filter(Vector v, String[] missingValues) {
        if (!v.isNominal()) {
            throw new IllegalArgumentException("Vector is not isNominal.");
        }
        for (int i = 0; i < v.getRowCount(); i++) {
            String value = v.getLabel(i);
            for (String missingValue : missingValues) {
                if (value.equals(missingValue)) {
                    v.setMissing(i);
                    break;
                }
            }
        }
        return v;
    }
}
