package rapaio.filters;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;

import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterGroupByNominal {

    public Frame[] groupByNominal(Frame df, int nominalIndex) {
        if (!df.getCol(nominalIndex).isNominal()) {
            throw new IllegalArgumentException("Index does not specify a isNominal attribute");
        }
        int len = df.getCol(nominalIndex).dictionary().length;
        ArrayList<Integer>[] mappings = new ArrayList[len];
        for (int i = 0; i < len; i++) {
            mappings[i] = new ArrayList<>();
        }
        for (int i = 0; i < df.getRowCount(); i++) {
            mappings[df.getCol(nominalIndex).getIndex(i)].add(i);
        }
        Frame[] frames = new Frame[len];
        for (int i = 0; i < frames.length; i++) {
            if (mappings[i].isEmpty()) {
                continue;
            }
            frames[i] = new MappedFrame(df, mappings[i]);
        }
        return frames;
    }
}
