/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.core.tests;

import rapaio.core.distributions.Distribution;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Interface for a hypothesis test
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/14/16.
 */
public interface HTest extends Printable {

    enum Alternative {
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

    double pValue();

    /**
     * Upper end of the confidence interval for the estimated quantity at confidence
     * level {@code 1 - sl}. For a {@link Alternative#LESS_THAN} alternative the interval
     * is one-sided, {@code (-Inf, ciHigh]}; for {@link Alternative#GREATER_THAN} it is
     * {@code [ciLow, +Inf)}, as in R's {@code t.test}. Tests without an interval return NaN.
     */
    double ciHigh();

    /**
     * Lower end of the confidence interval, see {@link #ciHigh()}.
     */
    double ciLow();

    /**
     * Confidence interval for a location estimate whose sampling distribution,
     * centered at the estimate, is {@code sampling}. Returns {@code {ciLow, ciHigh}}:
     * the central {@code 1 - sl} interval for a two tailed alternative, and the
     * one-sided {@code 1 - sl} interval matching the direction of a one tailed
     * alternative ({@code [q(sl), +Inf)} for greater, {@code (-Inf, q(1 - sl)]} for less).
     *
     * @param sampling sampling distribution of the estimator, located at the estimate
     * @param sl       significance level
     * @param alt      alternative hypothesis
     * @return two element array {@code {ciLow, ciHigh}}
     */
    static double[] confidenceInterval(Distribution sampling, double sl, Alternative alt) {
        return switch (alt) {
            case GREATER_THAN -> new double[] {sampling.quantile(sl), Double.POSITIVE_INFINITY};
            case LESS_THAN -> new double[] {Double.NEGATIVE_INFINITY, sampling.quantile(1 - sl)};
            case TWO_TAILS -> new double[] {sampling.quantile(sl / 2), sampling.quantile(1 - sl / 2)};
        };
    }

    @Override
    default String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    default String toFullContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }
}
