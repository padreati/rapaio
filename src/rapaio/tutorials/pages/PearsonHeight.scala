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
import rapaio.datasets.Datasets
import rapaio.core.stat.{Quantiles, Variance, Mean}
import rapaio.core.distributions.Normal
import rapaio.graphics._
import rapaio.core.correlation.PearsonRCorrelation

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class PearsonHeight extends TutorialPage {
  def pageName: String = "ExplorePearsonFatherSon"

  def pageTitle: String = "Explore Pearson Father Son Data"

  def render() {

    heading(1, "Analysis of Pearson's Height data set")

    val df = Datasets.loadPearsonHeightDataset

    p(
      f"""
            |This exploratory analysis is provided as a sample of analysis produced with Rapaio system.
            |//
            |The studied data set contains ${df.rowCount} observations and has ${df.colCount} columns.
          """.stripMargin)

    df.summary()

    heading(2, "Distribution of Measurements")

    p(
      """
        |We take a look at the histograms for the two dimensions.
      """.stripMargin)

    for (i <- 0 until df.colCount) {
      val normal = new Normal(Mean(df.col(i)).value, math.sqrt(Variance(df.col(i)).value))

      plot(xLim = (57, 80), yLim = (0.0, 0.2), xLab = df.colNames(i))
      histogram(df.col(i), bins = 23, min = 57, max = 80, prob = true)
      function(normal.pdf, col = 2)
      draw(700, 300)
    }

    heading(2, "About normality")

    p(
      """
        |Looking at both produced histograms we are interested to understand
        |if the data contained in both variables resemble a normal curve.
        |Basically we are interested if the the values of those dimensions
        |are normally distributed.
        |//
        |An usual graphical tools which can give us insights about that fact
        |is the quantile-quantile plot.
      """.stripMargin)

    for (i <- 0 until df.colCount) {
      qqplot(df.col(i), Normal(), yLab = df.colNames(i))
      draw(500, 300)
    }

    Mean(df.col("Father")).summary()
    Variance(df.col("Father")).summary()
    Mean(df.col("Son")).summary()
    Variance(df.col("Son")).summary()

    PearsonRCorrelation(Array(df.col("Father"), df.col("Son")), Array("Father", "Son")).summary()

    val perc = new Array[Double](11)
    for (i <- 0 until perc.length) {
      perc(i) = i.toDouble / 10.0
    }

    val fatherQuantiles = Quantiles(df.col("Father"), perc)
    val sonQuantiles = Quantiles(df.col("Son"), perc)
    fatherQuantiles.summary()
    sonQuantiles.summary()

    plot(xLim = (55, 80), yLim = (55, 80))
    for (i <- 0 until fatherQuantiles.values.length) {
      hl(fatherQuantiles.values(i), col = 30)
    }

    for (i <- 0 until sonQuantiles.values.length) {
      vl(sonQuantiles.values(i), col = 30)
    }
    points(df.col("Father"), df.col("Son"))

    draw(600, 600)

    p(">>>This tutorial is generated with Rapaio document printer facilities.<<<")
  }
}