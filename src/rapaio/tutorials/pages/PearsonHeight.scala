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
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class PearsonHeight extends TutorialPage {
  def pageName: String = "ExplorePearsonFatherSon"

  def pageTitle: String = "Explore Pearson Father Son Data"

  def render {
    //    heading(1, "Analysis of Pearson's Height dataset")
    //    val df: Frame = Datasets.loadPearsonHeightDataset
    //    p("This exploratory analysis is provided as a sample of analysis produced with Rapaio system.")
    //    p("The studied data set contains " + df.getRowCount + " observations and has " + df.getColCount + " columns.")
    //    Summary.summary(df)
    //    p("First we take a look at the histograms for the two dimensions") {
    //      var i: Int = 0
    //      while (i < df.getColCount) {
    //        {
    //          val normal: Nothing = new Nothing(new Mean(df.getCol(i)).getValue, sqrt(new Variance(df.getCol(i)).getValue))
    //          draw(new Plot().add(new Histogram(df.getCol(i), 23, true, 57, 80)).add(new Nothing(normal.getPdfFunction).setColorIndex(2)).setBottomLabel(df.getColNames(i)).setXRange(57, 80).setYRange(0, 0.20), 700, 300)
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    heading(2, "About normality")
    //    p("Looking at both produced histograms we are interested to understand " + "if the data contained in both variables resemble a normal " + "curve. Basically we are interested if the the values of " + "those dimensions are normally distributed.")
    //    p("An ususal graphical tools which can give us insights about that fact " + "is the quantile-quantile plot. ") {
    //      var i: Int = 0
    //      while (i < df.getColCount) {
    //        {
    //          val col: Nothing = df.getCol(i)
    //          val colIndex: Int = i
    //          val mu: Double = new Mean(col).getValue
    //          val normal: Nothing = new Nothing
    //          draw(new Nothing().add(col, normal).setLeftLabel(df.getColNames(colIndex)), 500, 300)
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    summary(new Mean(df.getCol("Father")))
    //    summary(new Variance(df.getCol("Father")))
    //    summary(new Mean(df.getCol("Son")))
    //    summary(new Variance(df.getCol("Son")))
    //    summary(new Nothing(df.getCol("Father"), df.getCol("Son")))
    //    val perc: Array[Double] = new Array[Double](11) {
    //      var i: Int = 0
    //      while (i < perc.length) {
    //        {
    //          perc(i) = i / (10.)
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    val fatherQuantiles: Quantiles = new Quantiles(df.getCol("Father"), perc)
    //    val sonQuantiles: Quantiles = new Quantiles(df.getCol("Son"), perc)
    //    summary(fatherQuantiles)
    //    summary(sonQuantiles)
    //    val plot: Plot = new Plot().setXRange(55, 80).setYRange(55, 80) {
    //      var i: Int = 0
    //      while (i < fatherQuantiles.getValues.length) {
    //        {
    //          plot.add(new Nothing(fatherQuantiles.getValues(i), false).setColorIndex(30))
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    } {
    //      var i: Int = 0
    //      while (i < sonQuantiles.getValues.length) {
    //        {
    //          plot.add(new Nothing(sonQuantiles.getValues(i), true).setColorIndex(30))
    //        }
    //        ({
    //          i += 1;
    //          i - 1
    //        })
    //      }
    //    }
    //    plot.add(new Points(df.getCol("Father"), df.getCol("Son")))
    //    draw(plot, 600, 600)
    //    p(">>>This tutorial is generated with Rapaio document printer facilities.<<<")
  }
}