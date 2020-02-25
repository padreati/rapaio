package rapaio.data;

import java.io.Serializable;
import java.util.List;

/**
 * Mapping function between a set of values and a dense integer value. Dense integer
 * index values are positive values
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/20.
 */
public interface Index<T> extends Serializable {

    int size();

    boolean containsValue(T value);

    boolean containsValue(Var v, int row);

    boolean containsValue(Frame df, String varName, int row);

    int getIndex(T value);

    int getIndex(Var v, int row);

    int getIndex(Frame df, String varName, int row);

    List<Integer> getIndexList(Var v);

    List<Integer> getIndexList(Frame df, String varName);

    T getValue(int pos);

    T getValue(Var v, int row);

    T getValue(Frame df, String varName, int row);

    List<T> getValues();

    List<T> getValueList(Var v);

    List<T> getValueList(Frame df, String varName);

    String getValueString(int pos);

    List<String> getValueStrings();

}

