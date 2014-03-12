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

package rapaio.graphics;

import rapaio.core.ColRange;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.graphics.base.AbstractFigure;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

import static rapaio.core.MathBase.max;
import static rapaio.core.MathBase.min;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoxPlot extends AbstractFigure {

	private final Vector[] vectors;
	private final String[] labels;

	public BoxPlot(Frame df) {
		this(df, null);
	}

	public BoxPlot(Vector v, String label) {
		vectors = new Vector[1];
		vectors[0] = v;
		labels = new String[1];
		labels[0] = label;
		initialize();
	}

	public BoxPlot(Vector numeric, Vector nominal) {
		labels = nominal.getDictionary();
		vectors = new Vector[labels.length];
		int[] count = new int[labels.length];
		for (int i = 0; i < numeric.getRowCount(); i++) {
			count[nominal.getIndex(i)]++;
		}
		for (int i = 0; i < count.length; i++) {
			vectors[i] = new Numeric(count[i]);
		}
		int[] pos = new int[vectors.length];
		for (int i = 0; i < nominal.getRowCount(); i++) {
			vectors[nominal.getIndex(i)].setValue(pos[nominal.getIndex(i)], numeric.getValue(i));
			pos[nominal.getIndex(i)]++;
		}
		initialize();
	}

	public BoxPlot(Vector[] vectors, String[] labels) {
		this.vectors = vectors;
		this.labels = labels;
		initialize();
	}

	public BoxPlot(Frame df, ColRange colRange) {
		if (colRange == null) {
			int len = 0;
			for (int i = 0; i < df.getColCount(); i++) {
				if (df.getCol(i).getType().isNumeric()) {
					len++;
				}
			}
			int[] indexes = new int[len];
			len = 0;
			for (int i = 0; i < df.getColCount(); i++) {
				if (df.getCol(i).getType().isNumeric()) {
					indexes[len++] = i;
				}
			}
			colRange = new ColRange(indexes);
		}
		List<Integer> indexes = colRange.parseColumnIndexes(df);
		vectors = new Vector[indexes.size()];
		labels = new String[indexes.size()];

		int pos = 0;
		for (int index : indexes) {
			vectors[pos] = df.getCol(index);
			labels[pos] = df.getColNames()[index];
			pos++;
		}

		initialize();
	}

	private void initialize() {
		setLeftMarkers(true);
		setLeftThicker(true);
		setBottomMarkers(true);
		setBottomThicker(true);
		setColorIndex(Vectors.newIdxOne(0));
	}

	@Override
	public Range buildRange() {
		Range range = new Range();
		range.union(0, Double.NaN);
		range.union(vectors.length, Double.NaN);
		for (Vector v : vectors) {
			for (int i = 0; i < v.getRowCount(); i++) {
				if (v.isMissing(i)) continue;
				range.union(Double.NaN, v.getValue(i));
			}
		}
		return range;
	}

	@Override
	public void buildLeftMarkers() {
		buildNumericLeftMarkers();
	}

	@Override
	public void buildBottomMarkers() {
		getParent().getBottomMarkersPos().clear();
		getParent().getBottomMarkersMsg().clear();

		double xspotwidth = getParent().getViewport().width / vectors.length;

		for (int i = 0; i < vectors.length; i++) {
			getParent().getBottomMarkersPos().add(i * xspotwidth + xspotwidth / 2);
			getParent().getBottomMarkersMsg().add(labels[i]);
		}
	}

	@Override
	public void paint(Graphics2D g2d, Rectangle rect) {
		super.paint(g2d, rect);

		for (int i = 0; i < vectors.length; i++) {
			Vector v = vectors[i];
			if (v.getRowCount() == 0) {
				continue;
			}
			double[] p = new double[]{0.25, 0.5, 0.75};
			double[] q = new Quantiles(v, p).getValues();
			double iqr = q[2] - q[0];
			double innerfence = 1.5 * iqr;
			double outerfence = 3 * iqr;

			double x1 = i + 0.5 - 0.3;
			double x2 = i + 0.5;
			double x3 = i + 0.5 + 0.3;

			g2d.setColor(getColor(i));
			// median
			g2d.setStroke(new BasicStroke(getLwd() * 2));
			g2d.draw(new Line2D.Double(
					xscale(x1), yscale(q[1]), xscale(x3), yscale(q[1])));

			// box
			g2d.setStroke(new BasicStroke(getLwd()));
			g2d.draw(new Line2D.Double(xscale(x1), yscale(q[0]), xscale(x3), yscale(q[0])));
			g2d.draw(new Line2D.Double(xscale(x1), yscale(q[2]), xscale(x3), yscale(q[2])));
			g2d.draw(new Line2D.Double(xscale(x1), yscale(q[0]), xscale(x1), yscale(q[2])));
			g2d.draw(new Line2D.Double(xscale(x3), yscale(q[0]), xscale(x3), yscale(q[2])));

			// outliers
			double upperwhisker = q[2];
			double lowerqhisker = q[0];
			for (int j = 0; j < v.getRowCount(); j++) {
				double point = v.getValue(j);
				if ((point > q[2] + outerfence) || (point < q[0] - outerfence)) {
					// big outlier
					int width = (int) (3 * getSize(i));
					g2d.fillOval(
							(int) xscale(x2) - width / 2 - 1,
							(int) yscale(point) - width / 2 - 1,
							width, width);
					continue;
				}
				if ((point > q[2] + innerfence) || (point < q[0] - innerfence)) {
					// outlier
					int width = (int) (3.5 * getSize(i));
					g2d.drawOval(
							(int) xscale(x2) - width / 2 - 1,
							(int) yscale(point) - width / 2 - 1,
							width, width);
					continue;
				}
				if ((point > upperwhisker) && (point < q[2] + innerfence)) {
					upperwhisker = max(upperwhisker, point);
				}
				if ((point < lowerqhisker) && (point >= q[0] - innerfence)) {
					lowerqhisker = min(lowerqhisker, point);
				}
			}

			// whiskers
			g2d.draw(new Line2D.Double(xscale(x1), yscale(upperwhisker), xscale(x3), yscale(upperwhisker)));
			g2d.draw(new Line2D.Double(xscale(x1), yscale(lowerqhisker), xscale(x3), yscale(lowerqhisker)));

			g2d.setStroke(new BasicStroke(getLwd(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{8}, 0));
			g2d.draw(new Line2D.Double(xscale(x2), yscale(q[2]), xscale(x2), yscale(upperwhisker)));
			g2d.draw(new Line2D.Double(xscale(x2), yscale(q[0]), xscale(x2), yscale(lowerqhisker)));
		}
	}

}
