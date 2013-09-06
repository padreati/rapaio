package rapaio.explore;

import rapaio.data.Frame;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author tutuianu
 */
public class DataContainer implements Serializable {

    private HashMap<String, Frame> frames = new HashMap<>();

    public Frame getFrame(String name) {
        return frames.get(name);
    }

    public Frame putFrame(String name, Frame df) {
        return frames.put(name, df);
    }

    public void dropFrames(String... names) {
        for (String name : names) {
            frames.remove(name);
        }
    }
}
