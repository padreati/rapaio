package rapaio.ml.nnet;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.*;
import rapaio.ml.Regressor;
import rapaio.workspace.Summary;


/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NeuralNetTest {

	@Test
	public void testAnd() {

		Vector a = new Numeric();
		Vector b = new Numeric();
		Vector and = new Numeric();

		a.addValue(0);
		b.addValue(0);
		and.addValue(0);

		a.addValue(1.);
		b.addValue(0.);
		and.addValue(0.);

		a.addValue(0.);
		b.addValue(1.);
		and.addValue(0.);

		a.addValue(1.);
		b.addValue(1.);
		and.addValue(1.);

		Frame df = new SolidFrame(and.getRowCount(), new Vector[]{and}, new String[]{"and"});
		df = Frames.addCol(df, b, "b", 0);
		df = Frames.addCol(df, a, "a", 0);

		Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 1}, 0.1).setRounds(100);

		for (int i = 0; i < 1000; i++) {
			nn.learn(df, "and");
		}
		nn.predict(df);

		Summary.lines(nn.getAllFitValues());

		Assert.assertTrue(nn.getFitValues().getValue(0) < .5);
		Assert.assertTrue(nn.getFitValues().getValue(1) < .5);
		Assert.assertTrue(nn.getFitValues().getValue(2) < .5);
		Assert.assertTrue(nn.getFitValues().getValue(3) > .5);
	}

	@Test
	public void testXor() {

		Vector a = new Numeric();
		Vector b = new Numeric();
		Vector xor = new Numeric();

		a.addValue(0);
		b.addValue(0);
		xor.addValue(1);

		a.addValue(1.);
		b.addValue(0.);
		xor.addValue(0.);

		a.addValue(0.);
		b.addValue(1.);
		xor.addValue(0.);

		a.addValue(1.);
		b.addValue(1.);
		xor.addValue(1.);

		Frame df = new SolidFrame(xor.getRowCount(), new Vector[]{xor}, new String[]{"xor"});
		df = Frames.addCol(df, b, "b", 0);
		df = Frames.addCol(df, a, "a", 0);

		Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 2, 1}, 0.1).setRounds(100);

		for (int i = 0; i < 1000; i++) {
			nn.learn(df, "xor");
		}
		nn.predict(df);

		Summary.lines(nn.getAllFitValues());

		Assert.assertTrue(nn.getFitValues().getValue(0) > .5);
		Assert.assertTrue(nn.getFitValues().getValue(1) < .5);
		Assert.assertTrue(nn.getFitValues().getValue(2) < .5);
		Assert.assertTrue(nn.getFitValues().getValue(3) > .5);
	}

	@Test
	public void testXorTwoOutputs() {

		Vector a = new Numeric();
		Vector b = new Numeric();
		Vector xorA = new Numeric();
		Vector xorB = new Numeric();

		a.addValue(0);
		b.addValue(0);
		xorA.addValue(0);
		xorB.addValue(1);

		a.addValue(1.);
		b.addValue(0.);
		xorA.addValue(1);
		xorB.addValue(0);

		a.addValue(0.);
		b.addValue(1.);
		xorA.addValue(1);
		xorB.addValue(0);

		a.addValue(1.);
		b.addValue(1.);
		xorA.addValue(0);
		xorB.addValue(1);

		Frame df = new SolidFrame(
				xorA.getRowCount(),
				new Vector[]{a, b, xorA, xorB},
				new String[]{"a", "b", "xorA", "xorB"});

		Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 4, 2}, 0.1).setRounds(100);

		for (int i = 0; i < 10_000; i++) {
			nn.learn(df, "xorA,xorB");
		}
		nn.predict(df);

		Assert.assertTrue(nn.getAllFitValues().getCol("xorA").getValue(0) < .5);
		Assert.assertTrue(nn.getAllFitValues().getCol("xorA").getValue(1) > .5);
		Assert.assertTrue(nn.getAllFitValues().getCol("xorA").getValue(2) > .5);
		Assert.assertTrue(nn.getAllFitValues().getCol("xorA").getValue(3) < .5);

		Assert.assertTrue(nn.getAllFitValues().getCol("xorB").getValue(0) > .5);
		Assert.assertTrue(nn.getAllFitValues().getCol("xorB").getValue(1) < .5);
		Assert.assertTrue(nn.getAllFitValues().getCol("xorB").getValue(2) < .5);
		Assert.assertTrue(nn.getAllFitValues().getCol("xorB").getValue(3) > .5);

		Summary.lines(nn.getAllFitValues());
	}

}
