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

package rapaio.experiment.image;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import static rapaio.math.MathTools.HALF_PI;
import static rapaio.math.MathTools.PI;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.core.stat.Quantiles;
import rapaio.data.VarDouble;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.util.DoubleComparators;
import rapaio.util.function.Double2DoubleFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/21.
 */
public class HoughTransform extends ParamSet<HoughTransform> {

    public static HoughTransform newTransform() {
        return new HoughTransform();
    }

    @Serial
    private static final long serialVersionUID = -7943372791156217527L;

    /**
     * Number of values to evaluate theta in Hough Space distributed equally in range 0 to 180.
     */
    public final ValueParam<Integer, HoughTransform> thetaSize = new ValueParam<>(this, 180, "thetaSize");
    /**
     * Number of values to evaluate distance (rho) to normal in Hough Space distributed equally in range [-d, d],
     * where d is the biggest diagonal of the image.
     */
    public final ValueParam<Integer, HoughTransform> rhoSize = new ValueParam<>(this, 100, "rhoSize");

    private double width;
    private double height;

    private Tensor<Double> hsMatrix;

    public Tensor<Double> getHsMatrix() {
        return hsMatrix;
    }

    public List<Line> getLines(double percentage) {
        List<Line> lines = new ArrayList<>();
        double[] values = hsMatrix.toDoubleArray();
        double qvalue = Quantiles.of(VarDouble.wrap(values), 1 - percentage).values()[0];
        double d = sqrt(width * width + height * height);
        for (int i = 0; i < hsMatrix.dim(0); i++) {
            for (int j = 0; j < hsMatrix.dim(1); j++) {
                double count = hsMatrix.get(i, j);
                if (count > qvalue) {
                    double theta = j * PI / thetaSize.get();
                    double rho = 2 * d * i / rhoSize.get() - d;
                    lines.add(new Line(count, theta, rho));
                }
            }
        }
        lines.sort((o1, o2) -> DoubleComparators.OPPOSITE_COMPARATOR.compare(o1.count, o2.count));
        return lines;
    }

    /**
     * Fit a binary raster with given height and width and data compressed into a {@code BitSet}.
     * The bit set has the size equal with {@code width * height} and has the flipped bits on
     * points which are considered on the image's edges (eventually obtained through an edge
     * detection algorithm).
     *
     * @param width  image's width
     * @param height image's height
     * @param data   bit set data of the binary image as a continuous array ordered by rows.
     */
    public HoughTransform fit(int width, int height, BitSet data) {
        hsMatrix = Tensors.zeros(Shape.of(rhoSize.get(), thetaSize.get()));
        populateHoughSpace(hsMatrix, width, height, data);
        return this;
    }

    private void populateHoughSpace(Tensor<Double> hs, int width, int height, BitSet data) {
        this.width = width;
        this.height = height;

        double d = sqrt(width * width + height * height) / 2;
        int pos = 0;
        double x_half = width / 2.0;
        double y_half = height / 2.0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (data.get(pos++)) {
                    for (int k = 0; k < thetaSize.get(); k++) {
                        double theta = k * PI / thetaSize.get() - HALF_PI;
                        double rho = (j - x_half) * cos(theta) + (i - y_half) * sin(theta);
                        hs.incDouble(1, (int) floor((rho + d) * rhoSize.get() / (2 * d)), k);
                    }
                }
            }
        }
    }

    public record Line(double count, double theta, double rho) {

        public Line2D computeLine(Rectangle2D r) {
            double x0 = r.getMinX();
            double y0 = r.getMinY();
            double x1 = r.getMaxX();
            double y1 = r.getMaxY();

            List<Point2D> points = new ArrayList<>();

            Double2DoubleFunction fy = x -> (rho - x * cos(theta)) / sin(theta);
            Double2DoubleFunction fx = y -> (rho - y * sin(theta)) / cos(theta);

            if (abs(theta - PI / 2) < 1e-14) {
                // theta = PI/2 -> vertical line
                double y_hat = fy.apply(0.0);
                if (y_hat >= y0 && y_hat <= y1) {
                    return new Line2D.Double(x0, y_hat, x1, y_hat);
                }
            }
            if (abs(theta - PI) < 1e-14) {
                // theta = PI -> horizontal line
                double x_hat = fx.apply(0.0);
                if (x_hat >= x0 && x_hat <= x1) {
                    return new Line2D.Double(x_hat, y0, x_hat, y1);
                }
            }

            double fy_x0 = fy.apply(x0);
            double fy_x1 = fy.apply(x1);
            double fx_y0 = fx.apply(y0);
            double fx_y1 = fx.apply(y1);

            double v = (x1 - x0) * fy_x0 / (fy_x0 - fy_x1);
            if ((fy_x0 - y0) * (fy_x1 - y0) < 0) {
                points.add(new Point2D.Double(v,y0));
            }
            if ((fy_x0 - y1) * (fy_x1 - y1) < 0) {
                points.add(new Point2D.Double(v,y1));
            }
            double v1 = (y1 - y0) * fx_y0 / (fx_y0 - fx_y1);
            if ((fx_y0 - x0) * (fx_y1 - x0) < 0) {
                points.add(new Point2D.Double(x0,v1));
            }
            if ((fx_y0 - x1) * (fx_y1 - x1) < 0) {
                points.add(new Point2D.Double(x1,v1));
            }
            return new Line2D.Double(points.get(0), points.get(1));
        }
    }
}
