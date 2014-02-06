package rapaio.cluster.distance;

import rapaio.data.Frame;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Distance {
    public double getDistance(Frame from, int fromRow, Frame targetFrame, int targetRow);
}
