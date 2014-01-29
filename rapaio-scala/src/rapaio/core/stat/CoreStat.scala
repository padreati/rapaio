package rapaio.core.stat

import rapaio.data.NumericFeature

/**
 * Compensated version of arithmetic mean of values from a {@code Vector}.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
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
