package rapaio.ml.clustering;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rapaio.data.Frame;
import rapaio.data.VarInt;
import rapaio.printer.Printable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/31/20.
 */
@RequiredArgsConstructor
@Getter
public class ClusteringResult implements Printable {

    protected final ClusteringModel model;
    protected final Frame df;
    protected final VarInt assignment;

}
