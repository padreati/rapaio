package rapaio.core.stat

import rapaio.data.NumericFeature

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
object CoreStat {

  def mean(feat: NumericFeature): Double = {
    var sum = 0.0
    var count = 0.0
    feat.forEach(x => {
      sum += x
      count += 1
    })
    if (count == 0) {
      Double.NaN
    }
    sum /= count
    var t = 0.0
    feat.forEach(x => t += x - sum)
    sum + t / count
  }
}
