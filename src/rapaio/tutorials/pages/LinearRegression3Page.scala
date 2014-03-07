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


/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class LinearRegression3Page extends TutorialPage {

  def pageName: String = "LinearRegression"

  def pageTitle: String = "Linear Regression: Multiple linear regression"

  def render() {
    //    heading(3, "Linear Regression with vectors and matrices - part 3")
    //    p("This tutorial aims to present how one can do by hand " + "linear regression using only vectors and matrices " + "operations. For practical purposes it should be used " + "linear regression models. ")
    //    heading(4, "Multiple Linear Regression")
    //    val cars: Frame = BaseFilters.retainNumeric(Datasets.loadCarMpgDataset)
    //    Summary.summary(cars)
    //    new Nothing(cars).summary
    //    val mpg: Nothing = cars.getCol("mpg").asInstanceOf[Nothing]
    //    val disp: Nothing = cars.getCol("displacement").asInstanceOf[Nothing]
    //    val weight: Nothing = cars.getCol("weight").asInstanceOf[Nothing]
    //    val hp: Nothing = cars.getCol("horsepower").asInstanceOf[Nothing]
    //    draw(new Plot().add(new Points(mpg, hp).setColorIndex(cars.getCol("origin")).setPchIndex(1)).setBottomLabel("mpg").setLeftLabel("horsepower"))
    //    draw(new Plot().add(new Points(mpg, weight)).setBottomLabel("mpg").setLeftLabel("weight"))
    //    draw(new Plot().add(new Points(hp, weight)).setBottomLabel("horsepower").setLeftLabel("weight"))
  }
}