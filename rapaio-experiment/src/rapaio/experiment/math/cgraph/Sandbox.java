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

package rapaio.experiment.math.cgraph;

import static rapaio.graphics.opt.GOpts.fill;
import static rapaio.graphics.opt.GOpts.pch.circleFull;
import static rapaio.graphics.opt.GOpts.sz;

import java.awt.Color;
import java.util.function.BiFunction;

import rapaio.core.tools.Grid2D;
import rapaio.data.VarDouble;
import rapaio.graphics.Plotter;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.plot.Plot;
import rapaio.sys.WS;

public class Sandbox {

    public static void main(String[] args) {
//        main1();
//        main2();
        main3();
    }

    public static void main1() {


        BiFunction<Double, Double, Double> biFun = (_x, _y) -> Math.sin(_x) + Math.cos(_y);

        CompContext c = new CompContext();

        CNode x = c.var("x");
        CNode y = c.var("y");
        CNode t = c.add(c.sin(x), c.cos(y));

        x.value.set(0.5);
        y.value.set(1.0);

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

            double lr = 1e-1;
            if (Math.abs(x.grad.get()) > 1e4 || Math.abs(y.grad.get()) > 1e4) {
                break;
            }

            double vv = Math.abs(x.grad.get()) + Math.abs(y.grad.get());
            if (vv < 1e-10 || !Double.isFinite(vv)) {
                break;
            }

            x.value.add_(x.grad.get() * -lr);
            y.value.add_(y.grad.get() * -lr);

            xx.addDouble(x.value.get());
            yy.addDouble(y.value.get());

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

        CompContext c = new CompContext();

        var zero = c.cnst("0", 0.);
        var x = c.var("x");
        var y = c.var("y");
        var c1 = c.cnst("c1", 1.5);
        var c2 = c.cnst("c2", 2.25);
        var c3 = c.cnst("c3", 2.625);

        var f1 = c.pow(c.add(c.sub(c1, x), c.mul(x, y)), 2);
        var f2 = c.pow(c.add(c.sub(c2, x), c.mul(x, c.pow(y, 2))), 2);
        var f3 = c.pow(c.add(c.sub(c3, x), c.mul(x, c.pow(y, 3))), 2);

        var loss = c.add(c.add(f1, f2), f3);

        BiFunction<Double, Double, Double> biFun = (_x, _y) -> Math.pow(1.5 - _x + _x * _y, 2) +
                Math.pow(2.25 - _x + _x * Math.pow(_y, 2), 2) +
                Math.pow(2.625 - _x + _x * Math.pow(_y, 3), 2);

        x.value.set(2.0);
        y.value.set(2.0);

        VarDouble xx = VarDouble.empty().name("xx");
        VarDouble yy = VarDouble.empty().name("yy");

        for (int i = 0; i < 10_000; i++) {

            c.zeroGrad();
            c.forward(loss);
            c.backward(loss);


            System.out.println("iter: " + (i + 1));
            System.out.println(x);
            System.out.println(y);
            xx.addDouble(x.value.get());
            yy.addDouble(y.value.get());

            double lr = 1e-3;

            if (Math.abs(x.grad.get()) > 1e5 || Math.abs(y.grad.get()) > 1e5) {
                break;
            }

            double vv = Math.abs(x.grad.get()) + Math.abs(y.grad.get());
            if (vv < 1e-10 || !Double.isFinite(vv)) {
                break;
            }

            x.value.sub_(lr * x.grad().get());
            y.value.sub_(lr * y.grad.get());
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

        BiFunction<Double, Double, Double> biFun = (_x, _y) -> Math.pow(_x * _x + _y - 11, 2) + Math.pow(_x + _y * _y - 7, 2);

        CompContext c = new CompContext();

        var x = c.var("x");
        var y = c.var("y");

        var eleven = c.cnst("11", 11.);
        var seven = c.cnst("7", 7.);

        var t1 = c.pow(c.sub(c.add(c.pow(x, 2), y), eleven), 2);
        var t2 = c.pow(c.sub(c.add(x, c.pow(y, 2)), seven), 2);
        var loss = c.add(t1, t2);

        x.value.set(.3719051);
        y.value.set(-10.0);
        x.value.set(0.0);
        y.value.set(0.0);


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

            if (Math.abs(x.grad.get()) > 1e4 || Math.abs(y.grad.get()) > 1e4) {
                break;
            }
            double vv = Math.abs(x.grad.get()) + Math.abs(y.grad.get());
            if (vv < 1e-10 || !Double.isFinite(vv)) {
                break;
            }

            x.value.sub_(lr * x.grad.get());
            y.value.sub_(lr * y.grad.get());


            xx.addDouble(x.value.get());
            yy.addDouble(y.value.get());

        }

        double r = 5;
        Grid2D grid = Grid2D.fromFunction(biFun, -r, r, -r, r, 100);
        int levelCount = 100;
        double[] p = VarDouble.seq(0, 1, 1. / levelCount).elements();
        double[] levels = grid.quantiles(p);
        Plot plot = Plotter.isoCurves(grid, levels, GOpts.palette.hue(200, 0, grid));
        plot.points(xx, yy, sz(3), fill(Color.yellow), circleFull());
        WS.draw(plot);
    }
}
