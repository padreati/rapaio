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

package rapaio.core.tests;

import rapaio.printer.Printable;

/**
 * Interface for a hypothesis test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/14/16.
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

    double ciHigh();

    double ciLow();

    @Override
    default String toContent() {
        return toSummary();
    }

    @Override
    default String toFullContent() {
        return toSummary();
    }
}
