/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.core.distributions.empirical;

import rapaio.printer.Printable;

import java.io.Serializable;

/**
 * Kernel function used in kernel density estimator
 *
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface KFunc extends Printable, Serializable {

    /**
     * pdf of the kernel
     *
     * @param x         x coordinate of the point where is evaluated
     * @param x0        x coordinate of the kernel center
     * @param bandwidth band width of the kernel
     * @return pdf value for x0
     */
    double pdf(double x, double x0, double bandwidth);

    /**
     * Computes smallest x coordinate value for which the kernel function has an influence
     *
     * @param x        center point of the kernel
     * @param bandwidth bandwidth of the kernel
     * @return minimum value where is an influence
     */
    double minValue(double x, double bandwidth);

    /**
     * Computes largest x coordinate value for which the kernel function has an influence
     * @param x center point of the kernel
     * @param bandwidth bandwidth of the kernel
     * @return maximum value where is an influence
     */
    double maxValue(double x, double bandwidth);

    @Override
    default String fullContent() {
        return content();
    }

    @Override
    default String summary() {
        return content();
    }
}
