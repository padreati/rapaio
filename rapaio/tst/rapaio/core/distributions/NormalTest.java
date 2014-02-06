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
package rapaio.core.distributions;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.io.CsvPersistence;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NormalTest {

	private static final double ERROR = 1e-9;
	private Frame df;

	public NormalTest() throws IOException, URISyntaxException {
		CsvPersistence persistence = new CsvPersistence();
		persistence.setHasHeader(false);
		persistence.setColSeparator(' ');
		df = persistence.read(this.getClass(), "standard_normal.csv");
		df = BaseFilters.toNumeric(df);
	}

	@Test
	public void testStandardQuantile() {
		Normal d = new Normal();
		double step = 0.0001;
		double q = 0;
		int pos = 0;
		while (true) {
			if (pos == 0) {
				assertEquals(Double.NEGATIVE_INFINITY, d.quantile(q), ERROR);
				q += step;
				pos++;
				continue;
			}
			if (pos == 10000) {
				assertEquals(Double.POSITIVE_INFINITY, d.quantile(1.), ERROR);
				q += step;
				pos++;
				break;
			}
			assertEquals(df.getValue(pos, 0), d.quantile(q), ERROR);
			assertEquals(q, d.cdf(d.quantile(q)), ERROR);
			assertEquals(df.getValue(pos, 1), d.pdf(q), ERROR);
			q += step;
			pos++;
		}
	}

	@Test
	public void testExceptions() {
		Normal dist = new Normal();
		try {
			dist.cdf(Double.NaN);
			assertFalse(true);
		} catch (IllegalArgumentException ex) {
		}
		try {
			dist.cdf(Double.NEGATIVE_INFINITY);
			assertFalse(true);
		} catch (IllegalArgumentException ex) {
		}
		try {
			dist.cdf(Double.POSITIVE_INFINITY);
			assertFalse(true);
		} catch (IllegalArgumentException ex) {
		}
		try {
			dist.quantile(-1);
			assertFalse(true);
		} catch (IllegalArgumentException ex) {
		}
	}

	@Test
	public void testAttributes() {
		Normal distr = new Normal(1, 1);
		assertEquals(1., distr.getMu(), ERROR);
		assertEquals(1., distr.getVar(), ERROR);
	}
}