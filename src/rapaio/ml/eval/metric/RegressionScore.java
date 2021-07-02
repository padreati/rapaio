/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.eval.metric;

import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/28/20.
 */
public record RegressionScore(RegressionMetric metric, double value) implements Printable {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegressionScore{").append("metric=").append(metric.getName());
        sb.append(",value=").append(Format.floatFlex(value)).append("}");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(metric.getName()).append(": ").append(Format.floatFlex(value)).append("\n");
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }
}
