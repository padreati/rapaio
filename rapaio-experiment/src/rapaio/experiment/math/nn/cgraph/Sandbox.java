/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.experiment.math.nn.cgraph;

import static rapaio.graphics.opt.GOpts.fill;
import static rapaio.graphics.opt.GOpts.pch.circleFull;
import static rapaio.graphics.opt.GOpts.sz;

import java.awt.Color;
import java.util.function.BiFunction;

import rapaio.core.tools.Grid2D;
import rapaio.data.VarDouble;
import rapaio.experiment.math.nn.cgraph.operations.CompNode;
import rapaio.graphics.Plotter;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.plot.Plot;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensors;
import rapaio.printer.Format;
import rapaio.sys.WS;

public class Sandbox {

    public static void main(String[] args) {
//        main1();
//        main2();
//        main3();
        main4();
    }

    public static void main4() {
        Context c = new Context(DType.FLOAT);

        Variable x = c.newVar("x");
        Variable w = c.newVar("w");
        Variable b = c.newVar("b");

        CompNode t = c.add(c.vdot(x, w), b);

        x.assign(c.tmt().stride(Shape.of(2), 2, 3, 5));
        w.assign(c.tmt().stride(Shape.of(2), 3, 1, 1));
        b.assign(c.tmt().scalar(2.));

        c.zeroGrad();
        c.forward(t);
        c.backward(t);

        System.out.println(t);
        System.out.println(x);
        System.out.println(w);
        System.out.println(b);

    }

    public static void main1() {

        Context c = new Context(DType.DOUBLE);

        Variable x = c.newVar("x");
        Variable y = c.newVar("y");

        CompNode t = c.add(c.sin(x), c.cos(y));

        CompNode zero = c.newConst("0", Tensors.scalar(0.));

        BiFunction<Double, Double, Double> biFun = (_x, _y) -> Math.sin(_x) + Math.cos(_y);

        x.assign(Tensors.ofDouble().scalar(0.5));
        y.assign(Tensors.ofDouble().scalar(1.0));


        VarDouble xx = VarDouble.empty().name("xx");
        VarDouble yy = VarDouble.empty().name("yy");

        for (int i = 0; i < 10_000; i++) {

            c.zeroGrad();
            c.forward(t);
            c.backward(t);

            System.out.println("iter: " + (i + 1));
            System.out.println(x);
            System.out.println(y);
            System.out.println(t);

            System.out.println("cos(x)=" + Format.floatFlex(Math.cos(x.value().tensor().getDouble())));
            System.out.println("sin(x)=" + Format.floatFlex(Math.sin(x.value().tensor().getDouble())));
            System.out.println("cos(y)=" + Format.floatFlex(Math.cos(y.value().tensor().getDouble())));
            System.out.println("cos(y)=" + Format.floatFlex(Math.sin(y.value().tensor().getDouble())));

            double lr = 1e-1;

            if (x.adjoint().tensor().abs().sum().doubleValue() > 1e4 || y.adjoint().tensor().abs().sum().doubleValue() > 1e4) {
                break;
            }

            double vv = x.adjoint().tensor().abs().sum().doubleValue() + y.adjoint().tensor().abs().sum().doubleValue();
            if (vv < 1e-10 || !Double.isFinite(vv)) {
                break;
            }

            x.value().add_(x.adjoint().tensor().mul(-lr));
            y.value().add_(y.adjoint().tensor().mul(-lr));


            xx.addDouble(x.value().tensor().getDouble());
            yy.addDouble(y.value().tensor().getDouble());

        }

        double r = 5;
        Grid2D grid = Grid2D.fromFunction(biFun, -r, r, -r, r, 150);
        int levelCount = 30;
        double[] p = VarDouble.seq(0, 1, 1. / levelCount).elements();
        double[] levels = grid.quantiles(p);
        Plot plot = Plotter.isoCurves(grid, levels, GOpts.palette.hue(200, 0, grid));
        plot.points(xx, yy, sz(3), fill(Color.yellow), circleFull());
        WS.draw(plot);
    }

    public static void main2() {

        Context c = new Context(DType.DOUBLE);

        Constant zero = c.newConst("0", Tensors.ofDouble().scalar(0.));
        Variable x = c.newVar("x");
        Variable y = c.newVar("y");
        Constant c1 = c.newConst("c1", Tensors.ofDouble().scalar(1.5));
        Constant c2 = c.newConst("c2", Tensors.ofDouble().scalar(2.25));
        Constant c3 = c.newConst("c3", Tensors.ofDouble().scalar(2.625));

        CompNode f1 = c.pow(c.add(c.sub(c1, x), c.mul(x, y)), 2);
        CompNode f2 = c.pow(c.add(c.sub(c2, x), c.mul(x, c.pow(y, 2))), 2);
        CompNode f3 = c.pow(c.add(c.sub(c3, x), c.mul(x, c.pow(y, 3))), 2);

        CompNode t = c.add(c.add(f1, f2), f3);
        CompNode loss = t;

        BiFunction<Double, Double, Double> biFun = (_x, _y) -> Math.pow(1.5 - _x + _x * _y, 2) +
                Math.pow(2.25 - _x + _x * Math.pow(_y, 2), 2) +
                Math.pow(2.625 - _x + _x * Math.pow(_y, 3), 2);

        x.assign(Tensors.ofDouble().scalar(2.0));
        y.assign(Tensors.ofDouble().scalar(2.0));


        VarDouble xx = VarDouble.empty().name("xx");
        VarDouble yy = VarDouble.empty().name("yy");

        for (int i = 0; i < 10_000; i++) {

            c.zeroGrad();
            c.forward(loss);
            c.backward(loss);


            System.out.println("iter: " + (i + 1));
            System.out.println(x);
            System.out.println(y);
            xx.addDouble(x.value().tensor().getDouble());
            yy.addDouble(y.value().tensor().getDouble());

            double lr = 1e-3;

            if (x.adjoint().tensor().abs().sum().doubleValue() > 1e5 || y.adjoint().tensor().abs().sum().doubleValue() > 1e5) {
                break;
            }

            double vv = x.adjoint().tensor().abs().sum().doubleValue() + y.adjoint().tensor().abs().sum().doubleValue();
            if (vv < 1e-10 || !Double.isFinite(vv)) {
                break;
            }

            x.value().add_(x.adjoint().tensor().mul(-lr));
            y.value().add_(y.adjoint().tensor().mul(-lr));


        }

        double r = 5;
        Grid2D grid = Grid2D.fromFunction(biFun, -r, r, -r, r, 125);
        int levelCount = 30;
        double[] p = VarDouble.seq(0, 1, 1. / levelCount).elements();
        double[] levels = grid.quantiles(p);
        Plot plot = Plotter.isoCurves(grid, levels, GOpts.palette.hue(200, 0, grid));
        plot.points(xx, yy, sz(3), fill(Color.yellow), circleFull());
        WS.draw(plot);
    }

    public static void main3() {

        Context c = new Context(DType.DOUBLE);

        Variable x = c.newVar("x");
        Variable y = c.newVar("y");

        Constant eleven = c.newConst("11", Tensors.scalar(11.));
        Constant seven = c.newConst("7", Tensors.scalar(7.));

        CompNode t1 = c.pow(c.sub(c.add(c.pow(x, 2), y), eleven), 2);
        CompNode t2 = c.pow(c.sub(c.add(x, c.pow(y, 2)), seven), 2);
        CompNode t = c.add(t1, t2);

        CompNode loss = t;
        BiFunction<Double, Double, Double> biFun = (_x, _y) -> Math.pow(_x * _x + _y - 11, 2) + Math.pow(_x + _y * _y - 7, 2);

        x.assign(Tensors.ofDouble().scalar(.3719051));
        y.assign(Tensors.ofDouble().scalar(-10.0));


        VarDouble xx = VarDouble.empty().name("xx");
        VarDouble yy = VarDouble.empty().name("yy");

        for (int i = 0; i < 10_000; i++) {

            c.zeroGrad();
            c.forward(loss);
            c.backward(loss);

            System.out.println("iter: " + i);
            System.out.println(x);
            System.out.println(y);
            System.out.println(loss);

            double lr = 1e-3;

            if (x.adjoint().tensor().abs().sum().doubleValue() > 1e4 || y.adjoint().tensor().abs().sum().doubleValue() > 1e4) {
                break;
            }
            double vv = x.adjoint().tensor().abs().sum().doubleValue() + y.adjoint().tensor().abs().sum().doubleValue();
            if (vv < 1e-10 || !Double.isFinite(vv)) {
                break;
            }

            x.value().sub_(x.adjoint().tensor().mul(lr));
            y.value().sub_(y.adjoint().tensor().mul(lr));


            xx.addDouble(x.value().tensor().getDouble());
            yy.addDouble(y.value().tensor().getDouble());

        }

        double r = 6;
        Grid2D grid = Grid2D.fromFunction(biFun, -r, r, -r, r, 150);
        int levelCount = 100;
        double[] p = VarDouble.seq(0, 1, 1. / levelCount).elements();
        double[] levels = grid.quantiles(p);
        Plot plot = Plotter.isoCurves(grid, levels, GOpts.palette.hue(200, 0, grid));
        plot.points(xx, yy, sz(3), fill(Color.yellow), circleFull());
        WS.draw(plot);
    }
}
