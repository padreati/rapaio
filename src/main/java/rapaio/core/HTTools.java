/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
 *
 */

package rapaio.core;

import rapaio.core.tests.TTestOneSample;
import rapaio.core.tests.ZTestOneSample;
import rapaio.core.tests.ZTestTwoPaired;
import rapaio.core.tests.ZTestTwoSamples;
import rapaio.data.Var;

/**
 * Hypothesis test tools class.
 * This class is an utility class which offers alternative shortcut methods to perform hypothesis testing.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/27/16.
 */
public class HTTools {

    public enum Alternative {
        TWO_TAILS("P > |z|"),
        GREATER_THAN("P > z"),
        LESS_THAN("P < z");

        private final String pCondition;

        Alternative(String pCondition) {
            this.pCondition = pCondition;
        }

        public String pCondition() {
            return pCondition;
        }
    }

    /**
     * One sample z test with default significance level 0.05 and to tails alternative
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param mean       mean of X
     * @param sd         standard deviation of X
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample zTestOneSample(double sampleMean, int sampleSize, double mean, double sd) {
        return new ZTestOneSample(sampleMean, sampleSize, mean, sd, 0.05, Alternative.TWO_TAILS);
    }

    /**
     * One sample z test with default significance level of 0.05 and two tails alternative
     *
     * @param x    given sample
     * @param mean mean of X
     * @param sd   standard deviation of X
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample zTestOneSample(Var x, double mean, double sd) {
        return new ZTestOneSample(x, mean, sd, 0.05, HTTools.Alternative.TWO_TAILS);
    }


    /**
     * One sample z test
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param mean       mean of X
     * @param sd         standard deviation of X
     * @param sl         significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt        alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample zTestOneSample(double sampleMean, int sampleSize, double mean, double sd, double sl, HTTools.Alternative alt) {
        return new ZTestOneSample(sampleMean, sampleSize, mean, sd, sl, alt);
    }

    /**
     * One sample z test
     *
     * @param x    given sample
     * @param mean mean of X
     * @param sd   standard deviation of X
     * @param sl   significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt  alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample zTestOneSample(Var x, double mean, double sd, double sl, HTTools.Alternative alt) {
        return new ZTestOneSample(x, mean, sd, sl, alt);
    }

    /**
     * Two unpaired samples z test for difference of the means with default values
     * for significance level (0.05) and alternative (two tails)
     *
     * @param xSampleMean sample mean for the first sample
     * @param xSampleSize sample size for the first sample
     * @param ySampleMean sample mean for the second sample
     * @param ySampleSize sample size for the second sample
     * @param mean        null hypothesis mean
     * @param xSd         standard deviation of the first sample
     * @param ySd         standard deviation of the second sample
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoSamples zTestTwoSamples(double xSampleMean, int xSampleSize, double ySampleMean, int ySampleSize, double mean, double xSd, double ySd) {
        return new ZTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd, ySd, 0.05, Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples z test for difference of the means
     *
     * @param xSampleMean sample mean for the first sample
     * @param xSampleSize sample size for the first sample
     * @param ySampleMean sample mean for the second sample
     * @param ySampleSize sample size for the second sample
     * @param mean        null hypothesis mean
     * @param xSd         standard deviation of the first sample
     * @param ySd         standard deviation of the second sample
     * @param sl          significance level (usual value 0.05)
     * @param alt         alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoSamples zTestTwoSamples(double xSampleMean, int xSampleSize, double ySampleMean, int ySampleSize, double mean, double xSd, double ySd, double sl, HTTools.Alternative alt) {
        return new ZTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd, ySd, sl, alt);
    }

    /**
     * Two unpaired samples z test for difference of the means with default values
     * for significance level (0.05) and alternative (two tails)
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param xSd  standard deviation of the first sample
     * @param ySd  standard deviation of the second sample
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoSamples zTestTwoSamples(Var x, Var y, double mean, double xSd, double ySd) {
        return new ZTestTwoSamples(x, y, mean, xSd, ySd, 0.05, Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples z test for difference of the means
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param xSd  standard deviation of the first sample
     * @param ySd  standard deviation of the second sample
     * @param sl   significance level (usual value 0.05)
     * @param alt  alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoSamples zTestTwoSamples(Var x, Var y, double mean, double xSd, double ySd, double sl, HTTools.Alternative alt) {
        return new ZTestTwoSamples(x, y, mean, xSd, ySd, sl, alt);
    }

    /**
     * Two paired samples z test for mean of the difference with default values
     * for significance level (0.05) and alternative (two tails)
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param sd   standard deviation of the first sample
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoPaired zTestTwoPaired(Var x, Var y, double mean, double sd) {
        return new ZTestTwoPaired(x, y, mean, sd, 0.05, Alternative.TWO_TAILS);
    }

    /**
     * Two paired samples z test for mean of the differences
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param sd   standard deviation of the first sample
     * @param sl   significance level (usual value 0.05)
     * @param alt  alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoPaired zTestTwoPaired(Var x, Var y, double mean, double sd, double sl, HTTools.Alternative alt) {
        return new ZTestTwoPaired(x, y, mean, sd, sl, alt);
    }


    /**
     * One sample t test with default significance level 0.05 and two tails alternative
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param sampleSd   sample standard deviation of X
     * @param mean       mean of X
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample tTestOneSample(double sampleMean, int sampleSize, double sampleSd, double mean) {
        return new TTestOneSample(sampleMean, sampleSize, sampleSd, mean, 0.05, Alternative.TWO_TAILS);
    }

    /**
     * One sample t test with default significance level of 0.05 and two tails alternative
     *
     * @param x    given sample
     * @param mean mean of X
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample tTestOneSample(Var x, double mean) {
        return new TTestOneSample(x, mean, 0.05, HTTools.Alternative.TWO_TAILS);
    }


    /**
     * One sample t test
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param sampleSd   sample standard deviation
     * @param mean       mean of X
     * @param sl         significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt        alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample tTestOneSample(double sampleMean, int sampleSize, double sampleSd, double mean, double sl, HTTools.Alternative alt) {
        return new TTestOneSample(sampleMean, sampleSize, sampleSd, mean, sl, alt);
    }

    /**
     * One sample t test
     *
     * @param x    given sample
     * @param mean mean of X
     * @param sl   significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt  alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample tTestOneSample(Var x, double mean, double sl, HTTools.Alternative alt) {
        return new TTestOneSample(x, mean, sl, alt);
    }
}
