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
package rapaio.distributions;

import static rapaio.core.MathBase.pow;

/**
 * @author Aurelian Tutuianu
 */
@Deprecated
public class Poisson extends Distribution {

	private final double lambda;

	public Poisson(double lambda) {
		if (lambda <= 0.) {
			throw new IllegalArgumentException("Lambda parameter for Poisson distribution must have positive getValue.");
		}
		this.lambda = lambda;
	}

	@Override
	public String getName() {
		return "Poisson Distribution";
	}

	@Override
	public double pdf(double x) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public double cdf(double x) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public double quantile(double p) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public double min() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public double max() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public double mean() {
		return lambda;
	}

	@Override
	public double mode() {
		return lambda;
	}

	@Override
	public double variance() {
		return lambda;
	}

	@Override
	public double skewness() {
		return pow(lambda, -0.5);
	}

	@Override
	public double kurtosis() {
		return pow(lambda, -1);
	}
}
