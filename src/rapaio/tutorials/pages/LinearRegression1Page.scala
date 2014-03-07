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

package rapaio.tutorials.pages

import rapaio.workspace.Workspace._

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class LinearRegression1Page extends TutorialPage {

  def pageName: String = "LinearRegression1"

  def pageTitle: String = "Linear Regression: predict with a constant"

  def render() {
    heading(3, "Linear Regression with vectors and matrices - part 1")
    p("This tutorial aims to present how one can do by hand " + "linear regression using only vectors and matrices " + "operations. For practical purposes it should be used " + "linear regression models. ")
    heading(4, "Predict with a constant")
    p("We will use the well-known data set Father-Son Heights. This " + "data was collected by Karl Pearson and contains the heights " + "of the fathers and their full-grown sons, in England, circa 1900. ")
    p("This data set is used to study the relation between the fathers " + "and the sons heights. However for the purpose of this tutorial " + "we are interested only in the sons heights. ")
    //    val df: Frame = Datasets.loadPearsonHeightDataset
    //    Summary.summary(df)
    //    heading(4, "Question: Given collected sons heights, can we find a constant which " + "can be used for prediction? ")
    //    p("First we get the heights of the sons in a numeric vector. ")
    //    val sons: Nothing = df.getCol("Son").asInstanceOf[Nothing]
    //    code("\t\tFrame df = Datasets.loadPearsonHeightDataset();\n" + "\t\tNumeric sons = (Numeric) df.getCol(\"Son\");\n")
    //    p("We ")
    //    code("mean: " + mean(sons).getValue(0))
    //    code("variance: " + `var`(sons).getValue(0))
    //    code("sd: " + sd(sons).getValue(0))
  }
}