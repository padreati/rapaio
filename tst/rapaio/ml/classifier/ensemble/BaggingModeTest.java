package rapaio.ml.classifier.ensemble;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/20.
 */
@ExtendWith(MockitoExtension.class)
public class BaggingModeTest {

    private static final List<String> dictionary = List.of("?", "a", "b", "c");

    private List<ClassifierResult> results;
    private VarNominal classes;
    private Frame densities;

    @BeforeEach
    void beforeEach() {
        results = new ArrayList<>();
        results.add(buildResult(0, 0.6, 0, 0.4));
        results.add(buildResult(0, 0.5, 0.1, 0.4));
        results.add(buildResult(0, 0, 0.1, 0.9));

        classes = VarNominal.empty(1, "a", "b", "c");
        densities = SolidFrame.byVars(
                VarDouble.empty(1).name("?"),
                VarDouble.empty(1).name("a"),
                VarDouble.empty(1).name("b"),
                VarDouble.empty(1).name("c")
        );
    }

    @Test
    void votingTest() {
        BaggingMode.VOTING.computeDensity(dictionary, results, classes, densities);
        assertEquals("a", classes.getLabel(0));
    }

    @Test
    void distributionTest() {
        BaggingMode.DISTRIBUTION.computeDensity(dictionary, results, classes, densities);
        assertEquals(1.1 / 3, densities.getDouble(0, "a"));
        assertEquals(0.2 / 3, densities.getDouble(0, "b"));
        assertEquals(1.7 / 3, densities.getDouble(0, "c"), 1e-12);

        assertEquals("c", classes.getLabel(0));
    }

    private ClassifierResult buildResult(double... probabilities) {

        ClassifierModel model = mock(ClassifierModel.class);
        Frame df = mock(Frame.class);

        when(model.targetNames()).thenReturn(new String[]{"y"});
        when(model.targetLevels(anyString())).thenReturn(dictionary);
        when(df.rowCount()).thenReturn(1);

        ClassifierResult result = ClassifierResult.build(model, df, true, true);
        VarNominal firstClasses = result.firstClasses();
        Frame firstDensity = result.firstDensity();

        int max = 0;
        double pMax = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > pMax) {
                max = i;
                pMax = probabilities[i];
            }
        }

        for (int i = 0; i < firstClasses.rowCount(); i++) {
            firstClasses.setLabel(i, dictionary.get(max));
            for (int j = 0; j < dictionary.size(); j++) {
                String label = dictionary.get(j);
                firstDensity.setDouble(i, label, probabilities[j]);
            }
        }

        return result;
    }
}
