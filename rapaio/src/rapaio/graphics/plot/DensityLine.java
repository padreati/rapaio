/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */
package rapaio.graphics.plot;

import rapaio.core.MathBase;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.distributions.empirical.KernelDensityEstimator;
import rapaio.distributions.empirical.KernelFunction;
import rapaio.distributions.empirical.KernelFunctionGaussian;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DensityLine extends PlotComponent {

	private final KernelDensityEstimator kde;
	private final int points;
	private final Vector vector;
	private final double bandwidth;

	public DensityLine(Vector vector) {
		this(vector, new KernelFunctionGaussian(), new KernelDensityEstimator(vector).getSilvermanBandwidth(vector), 256);
	}

	public DensityLine(Vector vector, double bandwidth) {
		this(vector, new KernelFunctionGaussian(), bandwidth, 256);
	}

	public DensityLine(Vector vector, KernelFunction kf, double bandwidth, int points) {
		this.kde = new KernelDensityEstimator(vector, kf, bandwidth);
		this.points = points;
		this.vector = vector;
		this.bandwidth = bandwidth;
	}

	@Override
	public Range buildRange() {
		double xmin = Double.NaN;
		double xmax = Double.NaN;
		double ymin = 0;
		double ymax = Double.NaN;

		for (int i = 0; i < vector.rowCount(); i++) {
			if (vector.isMissing(i))
				continue;
			if (xmin != xmin) {
				xmin = kde.getKernel().getMinValue(vector.value(i), bandwidth);
			} else {
				xmin = MathBase.min(xmin, kde.getKernel().getMinValue(vector.value(i), bandwidth));
			}
			if (xmax != xmax) {
				xmax = kde.getKernel().getMaxValue(vector.value(i), bandwidth);
			} else {
				xmax = MathBase.min(xmax, kde.getKernel().getMaxValue(vector.value(i), bandwidth));
			}
			if (ymax != ymax) {
				ymax = kde.getPdfFunction().eval(vector.value(i));
			} else {
				ymax = MathBase.min(ymax, kde.getPdfFunction().eval(vector.value(i)));
			}
		}
		// give some space
		ymax *= 1.05;
		Range range = new Range();
		range.setX1(xmin);
		range.setX2(xmax);
		range.setY1(ymin);
		range.setY2(ymax);
		return range;
	}

	@Override
	public void paint(Graphics2D g2d) {
		buildRange();
		Range range = getParent().getRange();
		Vector x = new Numeric(points + 1);
		Vector y = new Numeric(points + 1);
		double xstep = (range.getX2() - range.getX1()) / points;
		for (int i = 0; i < x.rowCount(); i++) {
			x.setValue(i, range.getX1() + i * xstep);
			y.setValue(i, kde.getPdfFunction().eval(x.value(i)));
		}

		for (int i = 1; i < x.rowCount(); i++) {
			if (range.contains(x.value(i - 1), y.value(i - 1)) && range.contains(x.value(i), y.value(i))) {
				g2d.setColor(getColor(i));
				g2d.setStroke(new BasicStroke(getLwd()));
				g2d.draw(new Line2D.Double(
						getParent().xscale(x.value(i - 1)),
						getParent().yscale(y.value(i - 1)),
						getParent().xscale(x.value(i)),
						getParent().yscale(y.value(i))));

			}
		}
	}
}
