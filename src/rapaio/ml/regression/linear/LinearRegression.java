/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.FFilter;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.regression.Regression;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegression extends RidgeRegression {

  /**
   * @param alpha
   */
  public LinearRegression() {
    super(0);
  }

  public static LinearRegression newLm() {
    return new LinearRegression();
  }

  private static final long serialVersionUID = 8610329390138787530L;

  @Override
  public LinearRegression withInputFilters(FFilter... filters) {
    return (LinearRegression) super.withInputFilters(filters);
  }

  @Override
  protected boolean coreTrain(Frame df, Var weights) {
    if (targetNames().length == 0) {
      throw new IllegalArgumentException("OLS must specify at least one target variable name");
    }
    RM X = SolidRM.copy(df.mapVars(inputNames()));
    RM Y = SolidRM.copy(df.mapVars(targetNames()));
    beta = QRDecomposition.from(X).solve(Y);
    return true;
  }

  @Override
  public Regression newInstance() {
    return new LinearRegression();
  }

  @Override
  public String name() {
    return "LinearRegression";
  }

}
