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
package rapaio.core;

/**
 * Interface for univariate functions.
 * <p/>
 * An univariate function is a function which takes one
 * real numeric argument and returns one real numeric getValue.
 * <p/>
 * It is mostly used as contract for various facilities which
 * needs a univariate function as input argument.
 * <p/>
 * Distributions exposes its probability functions
 * like probability density function or cumulative probability
 * function through this interface for late use.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface UnivariateFunction {

	double eval(double value);
}
