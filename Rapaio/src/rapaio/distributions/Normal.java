package rapaio.distributions;

import static rapaio.core.BaseMath.*;

/**
 * @author Aurelian Tutuianu
 */
public class Normal extends Distribution {

    private final double mu;
    private final double sd;
    private final double var;

    @Override
    public String getName() {
        return "Normal Distribution";
    }

    public Normal() {
        this(0, 1);
    }

    public Normal(double mu, double sd) {
        this.mu = mu;
        this.sd = sd;
        this.var = sd * sd;
    }

    public double getMu() {
        return mu;
    }

    public double getVar() {
        return var;
    }

    @Override
    public double pdf(double x) {
        return 1 / sqrt(2 * PI * var) * exp(-pow(x - mu, 2) / (2 * var));
    }

    @Override
    public double cdf(double x) {
        return cdf(x, mu, sd);
    }

    private double cdf(double x, double mu, double sd) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException("X is not a real number");
        }
        return cdfMarsaglia((x - mu) / sd);
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Inverse of a probability requires a probablity in the range [0,1], not " + p);
        }
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        //http://home.online.no/~pjacklam/notes/invnorm/
        double a[] = {
                -3.969683028665376e+01, 2.209460984245205e+02,
                -2.759285104469687e+02, 1.383577518672690e+02,
                -3.066479806614716e+01, 2.506628277459239e+00
        };

        double b[] = {
                -5.447609879822406e+01, 1.615858368580409e+02,
                -1.556989798598866e+02, 6.680131188771972e+01, -1.328068155288572e+01
        };

        double c[] = {
                -7.784894002430293e-03, -3.223964580411365e-01,
                -2.400758277161838e+00, -2.549732539343734e+00,
                4.374664141464968e+00, 2.938163982698783e+00
        };

        double d[] = {
                7.784695709041462e-03, 3.224671290700398e-01,
                2.445134137142996e+00, 3.754408661907416e+00
        };

        double p_low = 0.02425;
        double p_high = 1 - p_low;
        double result;

        if (0 < p && p < p_low) {
            double q = sqrt(-2 * log(p));
            result = (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5])
                    / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
        } else if (p_low <= p && p <= p_high) {
            double q = p - 0.5;
            double r = q * q;
            result = (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q
                    / (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1);
        } else//upper region
        {
            double q = sqrt(-2 * log(1 - p));
            result = -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5])
                    / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
        }

        //Refining step

        double e = cdf(result, 0, 1) - p;
        double u = e * sqrt(2 * PI) * exp(result * result / 2);
        result = result - u / (1 + result * u / 2);

        return result * sqrt(var) + mu;

    }

    private static double cdfMarsaglia(double x) {
        /*
         * Journal of Statistical Software (July 2004, Volume 11, Issue 5),
         * George Marsaglia Algorithum to compute the cdf of the normal
         * distribution for some z score
         */
        double s = x, t = 0, b = x, q = x * x, i = 1;
        while (s != t) {
            s = (t = s) + (b *= q / (i += 2));
        }
        return 0.5 + s * exp(-.5 * q - 0.91893853320467274178);
    }

    @Override
    public double min() {
        return Double.MIN_VALUE;
    }

    @Override
    public double max() {
        return Double.MAX_VALUE;
    }

    @Override
    public double mean() {
        return mu;
    }

    @Override
    public double mode() {
        return mean();
    }

    @Override
    public double variance() {
        return var;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return 0;
    }
}
