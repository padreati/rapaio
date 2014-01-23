package rapaio.ml.regression.nnet;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.*;


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

		Frame df = new SolidFrame(and.rowCount(), new Vector[]{and}, new String[]{"and"});
		df = Frames.addCol(df, b, "b", 0);
		df = Frames.addCol(df, a, "a", 0);

		MultiLayerPerceptronRegressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 20, 40, 1}, 0.1);

		for (int i = 0; i < 1000; i++) {
			nn.learn(df, "and", 100);
		}
		nn.predict(df);

//		Assert.assertEquals(0, nn.getFittedValues().value(0, 0), 1e-2);
//		Assert.assertEquals(0, nn.getFittedValues().value(1, 0), 1e-2);
//		Assert.assertEquals(0, nn.getFittedValues().value(2, 0), 1e-2);
//		Assert.assertEquals(1, nn.getFittedValues().value(3, 0), 1e-2);

		System.out.println(nn.getFittedValues().value(0, 0));
		System.out.println(nn.getFittedValues().value(1, 0));
		System.out.println(nn.getFittedValues().value(2, 0));
		System.out.println(nn.getFittedValues().value(3, 0));

		Assert.assertTrue(nn.getFittedValues().value(0, 0) < .5);
		Assert.assertTrue(nn.getFittedValues().value(1, 0) < .5);
		Assert.assertTrue(nn.getFittedValues().value(2, 0) < .5);
		Assert.assertTrue(nn.getFittedValues().value(3, 0) > .5);
	}
}
