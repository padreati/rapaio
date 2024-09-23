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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.core.tools;

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.util.collection.DoubleArrays;

public final class HistogramTable implements Printable {

    private final double min;
    private final double max;
    private final int bins;

    private final double step;
    private final double[] freq;

    public HistogramTable(Var v, double min, double max, int bins) {
        this.min = Double.isFinite(min) ? min : Minimum.of(v).value();
        this.max = Double.isFinite(max) ? max : Maximum.of(v).value();
        Var sel = v.stream().complete()
                .filter(x -> x.getDouble() >= this.min)
                .filter(x -> x.getDouble() <= this.max)
                .toMappedVar();

        this.bins = bins > 0 ? bins : computeFreedmanDiaconisEstimation(sel);
        this.step = (this.max - this.min) / this.bins;

        this.freq = new double[this.bins];
        sel.forEachDouble(x -> {
            int index = (int) Math.floor((x - this.min) / step);
            if (index >= this.bins) {
                index = this.bins - 1;
            }
            freq[index]++;
        });
    }

    private int computeFreedmanDiaconisEstimation(Var v) {
        double[] q = Quantiles.of(v, 0, 0.25, 0.75, 1).values();
        double iqr = q[2] - q[1];
        return (int) Math.min(1024, Math.ceil((q[3] - q[0]) / (2 * iqr * Math.pow(v.size(), -1.0 / 3.0))));
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public int bins() {
        return bins;
    }

    public Tensor<Double> freq() {
        return Tensors.stride(Shape.of(bins), freq);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HistogramTable{");
        sb.append("min=").append(Format.floatFlex(min)).append(",");
        sb.append("max=").append(Format.floatFlex(max)).append(",");
        sb.append("bins=").append(bins).append(",");
        sb.append("freq=[");
        int i = 0;
        for (; i < Math.min(12, bins); i++) {
            sb.append(freq[i]);
            if (i < bins - 1) {
                sb.append(",");
            }
        }
        if (i < bins) {
            sb.append("...");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        var pOpts = printer.getOptions().bind(options);
        return """
                HistogramTable
                ==============
                min=%s
                max=%s
                bins=%d
                freq=[
                %s]}""".formatted(
                pOpts.getFloatFormat().format(min),
                pOpts.getFloatFormat().format(max),
                bins,
                DoubleArrays.toContent(freq, printer, options)
        );
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        var pOpts = printer.getOptions().bind(options);
        return """
                HistogramTable
                ==============
                min=%s
                max=%s
                bins=%d
                freq=[
                %s]}""".formatted(
                pOpts.getFloatFormat().format(min),
                pOpts.getFloatFormat().format(max),
                bins,
                DoubleArrays.toContent(freq, printer, options)
        );
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        var pOpts = printer.getOptions().bind(options);
        return """
                HistogramTable
                ==============
                min=%s
                max=%s
                bins=%d
                freq=[
                %s]}""".formatted(
                pOpts.getFloatFormat().format(min),
                pOpts.getFloatFormat().format(max),
                bins,
                DoubleArrays.toFullContent(freq, printer, options)
        );
    }
}
