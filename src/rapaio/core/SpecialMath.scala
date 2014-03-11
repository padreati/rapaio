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

package rapaio.core

import rapaio.core.distributions.Normal

/**
 * Utility class which simplifies access to common java math utilities and also
 * enrich the mathematical operations set.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object SpecialMath {


  val Ln2 = math.log(2.0)

  /**
   * Returns the base 2 logarithm of a double value
   *
   * @param x the number from which we take base 2 logarithm
   * @return the base 2 logarithm of input getValue
   */
  def log2(x: Double): Double = math.log(x) / Ln2

  /**
   * Indicates if the number if finite and different than [[Double.NaN]].
   * <p/>
   * This function is used to check if a computation can produce finite results or not.
   * Another situation where is useful is when we test for a default numeric getValue which is usually set to [[Double.NaN]].
   *
   * @param x the number which needs to be verified
   * @return true if the number is finite and different than { @code Double.NaN}
   */
  def validNumber(x: Double): Boolean = !x.isNaN && !x.isInfinite

  def lnGamma(x: Double): Double = {
    var t: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    var p: Double = 0.0
    var p1: Double = 0.0
    var p2: Double = 0.0
    var p3: Double = 0.0
    var q: Double = 0.0
    var r: Double = 0.0
    var w: Double = 0.0
    var i: Int = 0
    val hx: Int = (java.lang.Double.doubleToLongBits(x) >> 32).asInstanceOf[Int]
    val lx: Int = java.lang.Double.doubleToLongBits(x).asInstanceOf[Int]
    val ix: Int = hx & 0x7fffffff
    if (ix >= 0x7ff00000) {
      Double.PositiveInfinity
    } else if ((ix | lx) == 0 || hx < 0) {
      Double.NaN
    } else if (ix < 0x3b900000) {
      -math.log(x)
    } else if ((((ix - 0x3ff00000) | lx) == 0) || (((ix - 0x40000000) | lx) == 0)) {
      r = 0
    } else if (ix < 0x40000000) {
      if (ix <= 0x3feccccc) {
        r = -math.log(x)
        if (ix >= 0x3FE76944) {
          y = 1 - x
          i = 0
        } else if (ix >= 0x3FCDA661) {
          y = x - (tc - 1)
          i = 1
        } else {
          y = x
          i = 2
        }
      } else {
        r = 0
        if (ix >= 0x3FFBB4C3) {
          y = 2.0 - x
          i = 0
        } else if (ix >= 0x3FF3B4C4) {
          y = x - tc
          i = 1
        } else {
          y = x - 1
          i = 2
        }
      }
      i match {
        case 0 =>
          z = y * y
          p1 = a0 + z * (a2 + z * (a4 + z * (a6 + z * (a8 + z * a10))))
          p2 = z * (a1 + z * (a3 + z * (a5 + z * (a7 + z * (a9 + z * a11)))))
          p = y * p1 + p2
          r += (p - 0.5 * y)
        case 1 =>
          z = y * y
          w = z * y
          p1 = t0 + w * (t3 + w * (t6 + w * (t9 + w * t12)))
          p2 = t1 + w * (t4 + w * (t7 + w * (t10 + w * t13)))
          p3 = t2 + w * (t5 + w * (t8 + w * (t11 + w * t14)))
          p = z * p1 - (tt - w * (p2 + y * p3))
          r += (tf + p)
        case 2 =>
          p1 = y * (u0 + y * (u1 + y * (u2 + y * (u3 + y * (u4 + y * u5)))))
          p2 = 1 + y * (v1 + y * (v2 + y * (v3 + y * (v4 + y * v5))))
          r += (-0.5 * y + p1 / p2)
      }
    }
    else if (ix < 0x40200000) {
      i = x.asInstanceOf[Int]
      y = x - i.asInstanceOf[Double]
      p = y * (s0 + y * (s1 + y * (s2 + y * (s3 + y * (s4 + y * (s5 + y * s6))))))
      q = 1 + y * (r1 + y * (r2 + y * (r3 + y * (r4 + y * (r5 + y * r6)))))
      r = 0.5 * y + p / q
      z = 1
      i match {
        case 7 =>
          z *= (y + 6.0)
        case 6 =>
          z *= (y + 5.0)
        case 5 =>
          z *= (y + 4.0)
        case 4 =>
          z *= (y + 3.0)
        case 3 =>
          z *= (y + 2.0)
          r += math.log(z)
      }
    }
    else if (ix < 0x43900000) {
      t = math.log(x)
      z = 1 / x
      y = z * z
      w = w0 + z * (w1 + y * (w2 + y * (w3 + y * (w4 + y * (w5 + y * w6)))))
      r = (x - 0.5) * (t - 1) + w
    }
    else {
      r = x * (math.log(x) - 1)
    }
    r
  }

  /**
   * Error function of a value.
   * <p/>
   * erf(x) = 2 * cdf(x sqrt(2)) -1
   * <p/>
   * where cdf is the cdf of the normal distribution
   * <p/>
   * http://en.wikipedia.org/wiki/Error_function
   *
   * @param x the number for which we compute erf
   * @return the erf of x
   */
  def erf(x: Double): Double = 2 * new Normal(0, 1).cdf(x * math.sqrt(2.0)) - 1

  /**
   * Inverse error function of a value.
   * <p/>
   * inverf(x) = invcdf(x/2+1/2)/sqrt(2)
   * <p/>
   * where invcdf is the inverse cdf of the normal distribution
   * <p/>
   * http://en.wikipedia.org/wiki/Error_function
   *
   * @param x the number for which we compute invErf
   * @return the invErf of x
   */
  def invErf(x: Double): Double = new Normal(0, 1).quantile(x / 2 + 0.5) / math.sqrt(2.0)

  /**
   * Complementary error function of a value.
   * <p/>
   * erfc(x) = 1 - erf(x)
   * <p/>
   * http://en.wikipedia.org/wiki/Error_function
   *
   * @param x the number for which we compute erf
   * @return the erf of x
   */
  def erfc(x: Double): Double = 2 * new Normal(0, 1).cdf(-x * math.sqrt(2.0))

  /**
   * Inverse of complementary error function of a double value.
   * inverfc(x) = invcdf(x/2)/-sqrt(2)
   * where invcdf is the inverse cdf of the normal distribution
   * <p/>
   * http://en.wikipedia.org/wiki/Error_function
   *
   * @param x the number for which we compute invErf
   * @return the invErf of x
   */
  def inverfc(x: Double): Double = new Normal(0, 1).quantile(x / 2) / -math.sqrt(2.0)

  /**
   * Computes the Beta function B(z,w).
   * <p/>
   * http://en.wikipedia.org/wiki/Beta_function
   *
   * @param z first argument getValue >= 0
   * @param w second argument getValue >= 0
   * @return beta function of z and w
   */
  def beta(z: Double, w: Double): Double = math.exp(lnBeta(z, w))

  /**
   * Computes natural logarithm of Beta function B(z, w).
   * <p/>
   * http://en.wikipedia.org/wiki/Beta_function
   *
   * @param z first argument getValue >= 0
   * @param w second argument getValue >= 0
   * @return lnBeta function of z and w
   */
  def lnBeta(z: Double, w: Double): Double = lnGamma(z) + lnGamma(w) - lnGamma(z + w)

  /**
   * Computes the regularized incomplete beta function, I<sub>x</sub>(a, b).
   * The result of which is always in the range [0, 1]
   *
   * @param x any getValue in the range [0, 1]
   * @param a any getValue >= 0
   * @param b any getValue >= 0
   * @return the result in a range of [0,1]
   */
  def betaIncReg(x: Double, a: Double, b: Double): Double = {
    require(a > 0 && b > 0, "a and b must be positive")
    require(x >= 0 && x <= 1, "x must be in the range [0,1]")
    if (x == 0 || x == 1) {
      return x
    }
    if (x > (a + 1) / (a + b + 2) || (1 - x) < (b + 1) / (a + b + 2)) {
      return 1 - betaIncReg(1 - x, b, a)
    }
    val numer: Double = a * math.log(x) + b * math.log(1 - x) - (math.log(a) + lnBeta(a, b))
    math.exp(numer) / lentz(Array[Double](x, a, b))
  }

  private def lentzA(pos: Int, args: Array[Double]): Double = {
    if (pos % 2 == 0) {
      val pos2 = pos / 2
      pos2 * (args(2) - pos2) * args(0) / ((args(1) + 2 * pos2 - 1) * (args(1) + 2 * pos2))
    } else {
      val pos2 = (pos - 1) / 2
      val num = -(args(1) + pos2) * (args(1) + args(2) + pos2) * args(0)
      val den = (args(1) + 2 * pos2) * (args(1) + 1 + 2 * pos2)
      num / den
    }
  }

  private def lentz(args: Array[Double]): Double = {
    var f_n: Double = 1.0
    var c_n: Double = .0
    var c_0: Double = f_n
    var d_n: Double = .0
    var d_0: Double = 0
    var delta: Double = 0
    var j: Int = 0
    while (math.abs(delta - 1) > 1e-15) {
      j += 1
      d_n = 1.0 + lentzA(j, args) * d_0
      if (d_n == 0.0) {
        d_n = 1e-30
      }
      c_n = 1.0 + lentzA(j, args) / c_0
      if (c_n == 0.0) {
        c_n = 1e-30
      }
      d_n = 1 / d_n
      delta = c_n * d_n
      f_n *= delta
      d_0 = d_n
      c_0 = c_n
    }
    f_n
  }

  private def betaIncRegFunc(x: Array[Double]): Double = betaIncReg(x(0), x(1), x(2)) - x(3)

  /**
   * Computes the inverse of the incomplete beta function,
   * I<sub>p</sub><sup>-1</sup>(a,b), such that [[betaIncReg( d o u b l e, d o u b l e, d o u b l e )]] I<sub>x</sub>(a, b)
   * } = <tt>p</tt>. The returned value, x, will always be in the range [0,1].
   * The input <tt>p</tt>, must also be in the range [0,1].
   *
   * @param p any getValue in the range [0,1]
   * @param a any getValue >= 0
   * @param b any getValue >= 0
   * @return the getValue x, such that { @link #betaIncReg(double, double, double) I<sub>x</sub>(a, b)
   *         } will return p.
   */
  def invBetaIncReg(p: Double, a: Double, b: Double): Double = {
    if (p < 0 || p > 1) {
      throw new ArithmeticException("The value p must be in the range [0,1], not" + p)
    }
    val eps: Double = 1e-15
    var maxIterations: Int = 1000
    var x1: Double = 0
    var x2: Double = 1
    val args: Array[Double] = Array[Double](p, a, b, p)
    args(0) = x1
    var fx1: Double = betaIncRegFunc(args)
    args(0) = x2
    var fx2: Double = betaIncRegFunc(args)
    val halfEps: Double = eps * 0.5
    if (fx1 * fx2 >= 0) {
      throw new ArithmeticException("The given interval does not appear to bracket the root")
    }
    var dif: Double = 1
    while (math.abs(x1 - x2) > eps && maxIterations > 0) {
      maxIterations -= 1
      val x3: Double = (x1 + x2) * 0.5
      args(0) = x3
      val fx3: Double = betaIncRegFunc(args)
      val x4: Double = x3 + (x3 - x1) * math.signum(fx1 - fx2) * fx3 / math.sqrt(fx3 * fx3 - fx1 * fx2)
      args(0) = x4
      val fx4: Double = betaIncRegFunc(args)
      if (fx3 * fx4 < 0) {
        x1 = x3
        fx1 = fx3
        x2 = x4
        fx2 = fx4
      }
      else if (fx1 * fx4 < 0) {
        dif = math.abs(x4 - x2)
        if (dif <= halfEps) {
          return x4
        }
        x2 = x4
        fx2 = fx4
      }
      else {
        dif = math.abs(x4 - x1)
        if (dif <= halfEps) {
          return x4
        }
        x1 = x4
        fx1 = fx4
      }
    }
    x2
  }

  private final val a0: Double = 7.72156649015328655494e-02
  private final val a1: Double = 3.22467033424113591611e-01
  private final val a2: Double = 6.73523010531292681824e-02
  private final val a3: Double = 2.05808084325167332806e-02
  private final val a4: Double = 7.38555086081402883957e-03
  private final val a5: Double = 2.89051383673415629091e-03
  private final val a6: Double = 1.19270763183362067845e-03
  private final val a7: Double = 5.10069792153511336608e-04
  private final val a8: Double = 2.20862790713908385557e-04
  private final val a9: Double = 1.08011567247583939954e-04
  private final val a10: Double = 2.52144565451257326939e-05
  private final val a11: Double = 4.48640949618915160150e-05
  private final val tc: Double = 1.46163214496836224576e+00
  private final val tf: Double = -1.21486290535849611461e-01
  private final val tt: Double = -3.63867699703950536541e-18
  private final val t0: Double = 4.83836122723810047042e-01
  private final val t1: Double = -1.47587722994593911752e-01
  private final val t2: Double = 6.46249402391333854778e-02
  private final val t3: Double = -3.27885410759859649565e-02
  private final val t4: Double = 1.79706750811820387126e-02
  private final val t5: Double = -1.03142241298341437450e-02
  private final val t6: Double = 6.10053870246291332635e-03
  private final val t7: Double = -3.68452016781138256760e-03
  private final val t8: Double = 2.25964780900612472250e-03
  private final val t9: Double = -1.40346469989232843813e-03
  private final val t10: Double = 8.81081882437654011382e-04
  private final val t11: Double = -5.38595305356740546715e-04
  private final val t12: Double = 3.15632070903625950361e-04
  private final val t13: Double = -3.12754168375120860518e-04
  private final val t14: Double = 3.35529192635519073543e-04
  private final val u0: Double = -7.72156649015328655494e-02
  private final val u1: Double = 6.32827064025093366517e-01
  private final val u2: Double = 1.45492250137234768737e+00
  private final val u3: Double = 9.77717527963372745603e-01
  private final val u4: Double = 2.28963728064692451092e-01
  private final val u5: Double = 1.33810918536787660377e-02
  private final val v1: Double = 2.45597793713041134822e+00
  private final val v2: Double = 2.12848976379893395361e+00
  private final val v3: Double = 7.69285150456672783825e-01
  private final val v4: Double = 1.04222645593369134254e-01
  private final val v5: Double = 3.21709242282423911810e-03
  private final val s0: Double = -7.72156649015328655494e-02
  private final val s1: Double = 2.14982415960608852501e-01
  private final val s2: Double = 3.25778796408930981787e-01
  private final val s3: Double = 1.46350472652464452805e-01
  private final val s4: Double = 2.66422703033638609560e-02
  private final val s5: Double = 1.84028451407337715652e-03
  private final val s6: Double = 3.19475326584100867617e-05
  private final val r1: Double = 1.39200533467621045958e+00
  private final val r2: Double = 7.21935547567138069525e-01
  private final val r3: Double = 1.71933865632803078993e-01
  private final val r4: Double = 1.86459191715652901344e-02
  private final val r5: Double = 7.77942496381893596434e-04
  private final val r6: Double = 7.32668430744625636189e-06
  private final val w0: Double = 4.18938533204672725052e-01
  private final val w1: Double = 8.33333333333329678849e-02
  private final val w2: Double = -2.77777777728775536470e-03
  private final val w3: Double = 7.93650558643019558500e-04
  private final val w4: Double = -5.95187557450339963135e-04
  private final val w5: Double = 8.36339918996282139126e-04
  private final val w6: Double = -1.63092934096575273989e-03
}