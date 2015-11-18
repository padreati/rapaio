package rapaio.core.distributions;

import static rapaio.core.MathTools.combinations;

/**
 * Created by andrei on 13.11.2015.
 */
public class Hypergeometric implements Distribution {

    private final int m;
    private final int n;
    private final int k;

    public Hypergeometric(int m, int n, int k) {
        if ( m < 0 ) {
            throw new IllegalArgumentException( " m parameter should not be negative" );
        }
        if ( n < 0 ) {
            throw new IllegalArgumentException( " n parameter should not be negative" );
        }
        if ( n + m < 1 ) {
            throw new IllegalArgumentException( " m + n should be at least 1." );
        }
        this.m = m;
        this.n = n;
        this.k = k;
    }


    @Override
    public String name() {
        return "Hypergeometric(m=" + m + ", n=" + n + ", k=" + k + ")";
    }

    @Override
    public boolean discrete() {
        return true;
    }

    @Override
    public double pdf( double x ) {
        if (x != Math.floor(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException( "x should be an integer since the hypergeometric" +
                    " repartition is a discrete repartion." );
        }
        double a = combinations(m, (int) x);
        double b = combinations(n, k - (int) x );
        double c = combinations(m + n, k );
        return a * b / c;
    }

    @Override
    public double cdf( double x ) {
        if (x != Math.floor(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException( "x should be an integer since the hypergeometric" +
                    " repartition is a discrete repartion." );
        }
        double cdf = 0;
        for ( int i = 0; i <= x; ++i ) {
            cdf += pdf(x);
        }
        return cdf;
    }

    @Override
    public double quantile( double p ) {
        double cdf = 0;
        if (p == 0) {
            return 0;
        }
        for ( int i = 0; i <= m; ++i ) {
            cdf += pdf(i);
            if ( cdf >= p ) {
                return i;
            }
        }
        return m;
    }

    @Override
    public double min() {
        return 0;
    }

    @Override
    public double max() {
        return m;
    }

    @Override
    public double mean() {
        return (m * k) / (m + n);
    }

    @Override
    public double mode() {
        return Math.floor( (k + 1) * (m + 1) / ( n + m + 2) );
    }

    /*
       According to http://mathworld.wolfram.com/HypergeometricDistribution.html
       the variance of a random variable X ~ Hypergeometric(m, n, k) is
       var(X) = ( n * m *  k * ( n + m - k ) ) / (( (n + m) ^ 2) * (n + m - 1))
     */
    @Override
    public double var() {
        return ( n * m *  k * ( n + m - k ) ) / (Math.pow( ( n + m ), 2 ) * (n + m - 1) );
    }

    @Override
    public double skewness() {
        return Math.sqrt( (n + m - 1) / (n * m * k * (n + m - k)) );
    }

    /*
       Computing the kurtosis using the formula from this Wikipedia page:
       https://en.wikipedia.org/wiki/Hypergeometric_distribution
     */

    @Override
    public double kurtosis() {
        double total = m + n;
        double firstTerm =  k * m * n * ( total - k) * ( total - 2) * ( total - 3);
        double secondTerm = (total - 1) * Math.pow( total, 2 ) * (total * (total + 1)
                            - 6 * m * n - 6 * k * (total - k)) + 6 * k * m * n * (total - k)
                            * (5 * total - 6);
        return secondTerm / firstTerm;
    }

    @Override
    public double entropy() {
        return Double.NaN;
    }
}
