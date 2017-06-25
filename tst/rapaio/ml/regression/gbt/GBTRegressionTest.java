
package rapaio.ml.regression.gbt;

import org.junit.Test;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.NumericVar;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.eval.RMSE;
import rapaio.graphics.Plotter;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;
import rapaio.ml.regression.boost.GBTRegression;
import rapaio.ml.regression.boost.gbt.GBTLossFunction;
import rapaio.ml.regression.simple.ConstantRegression;
import rapaio.ml.regression.tree.RTree;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.util.List;

import static rapaio.graphics.Plotter.lines;

public class GBTRegressionTest {



    @Test
    public void test() throws IOException {


        Frame df = Datasets.loadISLAdvertising().removeVars("ID");

        List<Frame> samples = SamplingTools.randomSampleSlices(df, 0.7);

        Frame tr = samples.get(0);
        Frame te = samples.get(1);

        NumericVar error = NumericVar.empty();

        RTree tree = RTree.buildCART()
                .withMaxDepth(4)
                .withMinCount(10);
        Regression gbt = new GBTRegression()
                .withInitRegressor(ConstantRegression.with(0))
                .withLossFunction(new GBTLossFunction.L2())
                .withRegressor(tree)
                .withShrinkage(0.005)
                .withSampler(RowSampler.subsampler(0.9))
                .withRuns(20)
                .withRunningHook((model, run) -> {

                    RFit fit = model.fit(te, false);
                    double err = new RMSE(te.getVar("Sales"), fit.firstFit()).getValue();
                    error.addValue(err);

                    if(run==1)
                        return;

                    WS.setPrinter(new IdeaPrinter());
                    WS.draw(lines(error).title("error " + WS.formatShort(err)));
                });

        gbt.train(tr, "Sales");
    }
}
