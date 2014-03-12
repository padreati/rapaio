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

import rapaio.core.RandomSource

import rapaio.workspace.Workspace._
import rapaio.core.distributions.DUniform
import rapaio.core.stat.StatOnline
import rapaio.data.Value
import rapaio.graphics._


/**
 * @author Aurelian Tutuianu
 */
class LawOfLargeNumbers extends TutorialPage {

  def pageName: String = "LawOfLargeNumbers"

  def pageTitle: String = "Explore Law of Large Numbers"

  def render() {
    RandomSource.seed(1)
    heading(2, "Simulation on Law of Large Numbers")

    p(
      """
        |In the probability theory the Law of the Large Numbers states that when
        |one repeats an experiment a large number of times, the expected
        |value of the average of the results of experiment
        |is the same with the expected value of the population.
        |//
        |in plain language it means that the average of the sample averages
        |will become closer to the population average as more trials are performed.
        |Additionally, the variance of this statistic decrease with the number of trials.
        |//
        |To illustrate the intuition behind this law we will consider our experiment
        |to be a run of rolls of a dice. A dice has 6 possible outcomes, the integer
        |numbers from 1 to 6, with each output having equal probability. Therefore the
        |expected value of a single die roll is
        |$$ E(X) = \sum_{i=1}^{6}p(x_i)x_i = \frac{1}{6}(1+2+3+4+5+6)=3.5 $$
        |//
        |We simulate the event of a single die roll to be a draw of a number from
        |the discrete uniform distribution with minimum value of 1 and
        |maximum value of 6. To simulate a large number of independent events
        |we simply draw a large sample of generated random numbers from this distribution.
        |//
        |Rapaio makes this possible by using the following code:
      """.stripMargin)

    code(
      """
        |    val N = 1000
        |    val events = DUniform(1, 6).sample(N)
      """.stripMargin)

    val N = 1000
    val events = DUniform(1, 6).sample(N)

    p(
      """
        |Thus we have stored in a vector N (1000) outputs of those events.
        |We compute the running mean using StatOnline:
      """.stripMargin)

    code(
      """
        |    val ocs = new StatOnline
        |    val mean = new Value(N)
        |    for (i <- 0 until events.rowCount) {
        |      ocs.update(events.values(i))
        |      mean.values(i) = ocs.mean
        |    }
      """.stripMargin)

    val ocs = new StatOnline
    val mean = new Value(N)
    for (i <- 0 until events.rowCount) {
      ocs.update(events.values(i))
      mean.values(i) = ocs.mean
    }


    p(
      """
        |Now we have the running mean stored in the vector mean and we can plot
        |how that running mean evolves as the size of the sample grows.
      """.stripMargin)



    plot(yLim = (2.5, 4.5))
    hl(3.5, lwd = 1.5f, col = 1)
    lines(x = Value(1, 1000, x => x), y = mean, lwd = 1.5f, col = 2)
    draw(800, 300)

    p(
      """
        |Thus we can clearly notice two fact from the plot above.
        |First fact is that the running average gets closer to the
        |expected getValue, as sample size grows.
        |Second fact is that deviation from expected getValue is smaller as
        |the sample size grows aka. smaller variation.
        |//
        |The code for drawing the plot follows:
      """.stripMargin)

    code(
      """
        |    plot(yLim = (2.5, 4.5))
        |    line(3.5, lwd = 1.5f, col = 1, h = true)
        |    lines(x = Value(1, 1000, x => x), y = mean, lwd = 1.5f, col = 2)
        |    draw(800, 300)
      """.stripMargin)

    p(">>>This tutorial is generated with Rapaio document printer facilities.<<<")
  }
}