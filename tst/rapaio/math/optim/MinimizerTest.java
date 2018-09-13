package rapaio.math.optim;

import org.junit.Test;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.solid.SolidVarDouble;
import rapaio.math.functions.R1Derivative;
import rapaio.math.functions.R1Function;
import rapaio.math.functions.R1Hessian;
import rapaio.math.functions.R2Derivative;
import rapaio.math.functions.R2Function;
import rapaio.math.functions.R2Hessian;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.functions.RHessian;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;
import rapaio.printer.idea.IdeaPrinter;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static rapaio.graphics.Plotter.lines;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/19/17.
 */
public class MinimizerTest {

    @Test
    public void basicGradientDescentTest() {
        double gamma = 2;

        RV init = SolidRV.wrap(1, gamma);

        RFunction f = x ->
                (pow(x.get(0), 2) + gamma * pow(x.get(1), 2)) / 2;
        RDerivative d1f = x ->
                SolidRV.wrap(x.get(0), gamma * x.get(1));

        GradientDescentMinimizer rf = new GradientDescentMinimizer(init, f, d1f, 100);
        report(rf, init, f);
    }

    @Test
    public void basicCoordinateDescentTest() {
        double gamma = 10;

        RV init = SolidRV.wrap(1, gamma);

        RFunction f = x ->
                (pow(x.get(0), 2) + gamma * pow(x.get(1), 2)) / 2;
        RDerivative d1f = x ->
                SolidRV.wrap(x.get(0), gamma * x.get(1));

        Minimizer rf = new CoordinateDescentMinimizer(init, f, d1f, 1000);
        report(rf, init, f);
    }

    @Test
    public void basicNewtonRaphsonMinimizerTest1() {
        double gamma = 10;

        RV init = SolidRV.wrap(1, gamma);

        // (x1^2 + gamma x2^2)/2
        RFunction f1 = x -> (pow(x.get(0), 2) + gamma * pow(x.get(1), 2)) / 2;

        // (x1, gamma x2)
        RDerivative d1f = x -> SolidRV.wrap(x.get(0), gamma * x.get(1));

        // (1, 0, 0, gamma)
        RHessian d2f = x -> SolidRM.copy(2, 2, 1, 0, 0, gamma);

        report(new NewtonRaphsonMinimizer(init, f1, d1f, d2f, 100), init, f1);

    }

    @Test
    public void basicNewtonRaphsonMinimizerTest2() {

        RV init = SolidRV.wrap(1.1);

        // e^x -2x^2
        R1Function f = new R1Function(x -> exp(x) - 2 * pow(x, 2));

        // e^x -4x
        R1Derivative d1f = new R1Derivative(x -> exp(x) - 4 * x);
        // e^x -4
        R1Hessian d2f = new R1Hessian(x -> exp(x) - 4);

        Minimizer minimizer = new NewtonRaphsonMinimizer(init, f, d1f, d2f, 10000);
        minimizer.compute();
//        report(minimizer, init, f);

        Var sol = minimizer.solutions().stream()
                .skip(10)
                .mapToDouble(s -> s.get(0)).boxed().collect(SolidVarDouble.collector());

        sol.printLines();
        WS.setPrinter(new IdeaPrinter());
        WS.draw(lines(VarInt.seq(sol.rowCount()), sol));

        VarDouble xx = SolidVarDouble.seq(-5, 3, 0.01);
        VarDouble yy = SolidVarDouble.from(xx, f::apply);
        VarDouble zz = SolidVarDouble.from(xx, value -> d1f.apply(value).get(0));
        VarDouble tt = SolidVarDouble.from(xx, value -> d2f.apply(value).get(0, 0));

//        WS.draw(lines(xx, yy)
//                .lines(xx, zz, color(1))
//                .lines(xx, tt, color(2))
//                .hLine(0)
//                .vLine(0)
//                .vLine(minimizer.solution().get(0)));

    }

    @Test
    public void minimizerConvexAnalysis_9_20() {

        RV init = SolidRV.wrap(1, 0);

        R2Function f = new R2Function((x, y) -> exp(x + 3 * y - 0.1) + exp(x - 3 * y - 0.1) + exp(-x - 0.1));

        R2Derivative d1f = new R2Derivative((x, y) ->
                SolidRV.wrap(
                        exp(x + 3 * y - 0.1) + exp(x - 3 * y - 0.1) + exp(-x - 0.1),
                        3 * exp(x + 3 * y - 0.1) - 3 * exp(x - 3 * y - 0.1)
                ));

        R2Hessian d2f = new R2Hessian((x, y) ->
                SolidRM.copy(2, 2,
                        exp(x + 3 * y - 0.1) + exp(x - 3 * y - 0.1) + exp(- -0.1),
                        3 * exp(x + 3 * y - 0.1) - 3 * exp(x - 3 * y - 0.1),

                        3 * exp(x + 3 * y - 0.1) - 3 * exp(x - 3 * y - 0.1),
                        9 * exp(x + 3 * y - 0.1) + 9 * exp(x - 3 * y - 0.1)
                ));

        report(new GradientDescentMinimizer(init, f, d1f, 100), init, f);
        report(new CoordinateDescentMinimizer(init, f, d1f, 100), init, f);
        report(new NewtonRaphsonMinimizer(init, f, d1f, d2f, 100), init, f);
    }

    private void report(Minimizer alg, RV init, RFunction f) {
        alg.compute();
        alg.printSummary();

        List<RV> sols = alg.solutions();

        List<VarDouble> vars = new ArrayList<>();
        for (int i = 0; i < init.count(); i++) {
            vars.add(SolidVarDouble.empty().withName("x" + i));
        }
        vars.add(SolidVarDouble.empty().withName("fx"));

        for (RV sol : sols) {
            for (int i = 0; i < init.count(); i++) {
                vars.get(i).addDouble(sol.get(i));
            }
            vars.get(init.count()).addDouble(f.apply(sol));
        }

        WS.setPrinter(new IdeaPrinter());
        WS.draw(lines(vars.get(init.count())));

        SolidFrame.byVars(vars).printLines();
    }
}
