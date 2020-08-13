package rapaio.util;

import lombok.Getter;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
@Getter
public class Triple<T1, T2, T3> implements Serializable {

    private static final long serialVersionUID = -8306687467822435475L;

    public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 v1, T2 v2, T3 v3) {
        return new Triple<>(v1, v2, v3);
    }

    private final T1 v1;
    private final T2 v2;
    private final T3 v3;

    private Triple(T1 v1, T2 v2, T3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
}
