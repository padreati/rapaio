
package rapaio.ml.classification.tree;

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.sample.StatSampling;
import rapaio.workspace.Summary;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3Test {

    @Test
    public void testBasicID3() throws IOException {
        Frame df = Datasets.loadMushrooms();
        final String className = "classes";

        List<Frame> frames = StatSampling.randomSample(df, new int[]{(int) (0.1 * df.getRowCount())});
        Frame tr = frames.get(0);
        Frame te = frames.get(1);
        
        ID3 id3 = new ID3();
        id3.learn(tr, className);
        id3.predict(te);
        
        Summary.summary(new ConfusionMatrix(te.getCol(className), id3.getPrediction()));
        Summary.summary(id3);
    }
}
