package rapaio.data;

import rapaio.data.stream.VSpot;
import rapaio.data.stream.VSpots;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractVar implements Var {

    private String name;
    private List<VSpot> spots;

    public String name() {
        return name;
    }

    public AbstractVar withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public VSpots stream() {
        if (spots == null || spots.size() != rowCount()) {
            spots = new LinkedList<>();
            for (int i = 0; i < this.rowCount(); i++) {
                spots.add(new VSpot(i, this));
            }
        }
        return new VSpots(spots.stream());
    }
}
