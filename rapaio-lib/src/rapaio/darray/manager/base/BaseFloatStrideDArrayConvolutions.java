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

package rapaio.darray.manager.base;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rapaio.darray.DArray;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.util.Pair;

public final class BaseFloatStrideDArrayConvolutions {

    public static DArray<Float> conv1d(DArray<Float> input, DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation,
            int groups) {

        // complete input shape
        if (input.rank() > 3) {
            throw new IllegalArgumentException("Input signal (this darray) must have at most 3 dimensions.");
        }
        while (input.rank() < 3) {
            // missing dimensions means sequence of length 1, a single channel and a batch of unit size
            input = input.stretch(0);
        }

        // complete kernel shape
        if (kernel.rank() > 3) {
            throw new IllegalArgumentException("Weight must have at most 3 dimensions.");
        }
        while (kernel.rank() < 3) {
            kernel = kernel.stretch(0);
        }

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inLen = input.dim(2);

        int outChannels = kernel.dim(0);
        int inDepth = kernel.dim(1);
        int k = kernel.dim(2);

        if (bias != null) {
            if (bias.rank() != 1) {
                throw new IllegalArgumentException("Bias must be a 1D tensor.");
            }
            if (bias.dim(0) != outChannels) {
                throw new IllegalArgumentException("Bias must have the same number of elements as output channels.");
            }
        }

        if (inChannels % groups != 0) {
            throw new IllegalArgumentException("Number of input channels must be a multiple of groups.");
        }
        if (outChannels % groups != 0) {
            throw new IllegalArgumentException("Number of output channels must be a multiple of groups.");
        }
        if (inDepth * groups != inChannels) {
            throw new IllegalArgumentException("Number of input channels divided by groups must be equal to kernel input depth.");
        }

        int outDepth = outChannels / groups;
        int outLen = Math.floorDiv(inLen + 2 * padding - (k - 1) * dilation, stride);

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, outChannels, outLen));

        for (int batch = 0; batch < n; batch++) {

            DArray<Float> inBatch = input.selsq(0, batch); // shape: (inChannels, inLen)
            DArray<Float> outBatch = output.selsq(0, batch); // shape: (outChannels, outLen)

            for (int group = 0; group < groups; group++) {

                DArray<Float> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth); // shape: (inDepth, inLen)
                DArray<Float> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth); // shape: (outDepth, outLen)

                DArray<?> kernelSlice = kernel.narrow(0, group * outDepth, (group + 1) * outDepth); // shape: (outDepth, inDepth, k)

                DArray<Float> unfold1d = inSlice.unfold1d(k, stride, padding, dilation);

                kernelSlice = kernelSlice.reshape(Shape.of(outDepth, inDepth * k));
                outSlice.add_(kernelSlice.mm(unfold1d));
            }
        }

        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getFloat(oc));
            }
        }
        return output;
    }

    public static DArray<Float> convTranspose1d(DArray<Float> input, DArray<?> weights, DArray<?> bias, int stride, int padding, int dilation,
            int groups, int outputPadding) {
        if (input.rank() > 3) {
            throw new IllegalArgumentException("Input signal (this darray) must have at most 3 dimensions.");
        }
        while (input.rank() < 3) {
            input = input.stretch(0);
        }
        if (weights.rank() > 3) {
            throw new IllegalArgumentException("Weight must have at most 3 dimensions.");
        }
        while (weights.rank() < 3) {
            weights = weights.stretch(0);
        }

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inLen = input.dim(2);
        int outDepth = weights.dim(1);
        int inDepth = inChannels / groups;
        int outChannels = outDepth * groups;
        int kLen = weights.dim(2);

        if (bias != null) {
            if (bias.rank() != 1) {
                throw new IllegalArgumentException("Bias must be a 1D tensor.");
            }
            if (bias.dim(0) != outChannels) {
                throw new IllegalArgumentException("Bias must have the same number of elements as output channels.");
            }
        }

        if (input.dim(1) != weights.dim(0)) {
            throw new IllegalArgumentException("Input channels and weight output channels do not match.");
        }
        if (inChannels % groups != 0) {
            throw new IllegalArgumentException("Number of input channels must be a multiple of groups.");
        }
        if (outChannels % groups != 0) {
            throw new IllegalArgumentException("Number of output channels must be a multiple of groups.");
        }

        int outLen = (inLen - 1) * stride - 2 * padding + (kLen - 1) * dilation + 1 + outputPadding;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, outChannels, outLen));

        for (int batch = 0; batch < n; batch++) {
            DArray<?> inBatch = input.selsq(0, batch);
            DArray<?> outBatch = output.selsq(0, batch);

            for (int group = 0; group < groups; group++) {
                DArray<?> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, inLen)
                DArray<?> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth); // (outDepth, outLen)
                DArray<?> kernelSlice = weights.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, outDepth, kLen)

                // col = kernelFlat^T @ inSlice: (outDepth*kLen, inDepth) @ (inDepth, inLen) = (outDepth*kLen, inLen)
                DArray<?> kernelFlat = kernelSlice.reshape(Shape.of(inDepth, outDepth * kLen)); // (inDepth, outDepth*kLen)
                DArray<?> col = kernelFlat.t().mm(inSlice); // (outDepth*kLen, inLen)

                // fold col (outDepth, kLen, inLen) -> outSlice (outDepth, outLen)
                for (int oc = 0; oc < outDepth; oc++) {
                    for (int k = 0; k < kLen; k++) {
                        for (int i = 0; i < inLen; i++) {
                            int outPos = i * stride + k * dilation - padding;
                            if (outPos >= 0 && outPos < outLen) {
                                outSlice.incFloat(col.getFloat(oc * kLen + k, i), oc, outPos);
                            }
                        }
                    }
                }
            }
        }
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getFloat(oc));
            }
        }
        return output;
    }

    public static DArray<Float> unfold1d(DArray<Float> in, int kLen, int stride, int padding, int dilation) {

        if (in.rank() != 2 && in.rank() != 3) {
            throw new IllegalArgumentException("Input must be a 2D or 3D array.");
        }

        boolean batched = in.rank() == 3;
        DArray<Float> input = batched ? in : in.stretch(0);

        int n = input.dim(0);
        int inCh = input.dim(1);

        int outLen = Math.floorDiv(input.dim(2) + 2 * padding - (kLen - 1) * dilation, stride);

        DArray<Float> result = input.dm().zeros(DType.FLOAT, Shape.of(n, inCh, kLen, outLen));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < inCh; c++) {
                for (int k = 0; k < kLen; k++) {
                    for (int o = 0; o < outLen; o++) {
                        int delta = o * stride + k * dilation - padding;
                        if (delta >= 0 && delta < input.dim(2)) {
                            result.setFloat(input.getFloat(b, c, delta), b, c, k, o);
                        }
                    }
                }
            }
        }
        result = result.reshape(Shape.of(n, inCh * kLen, outLen));
        if (!batched) {
            result = result.squeeze(0);
        }
        return result;
    }

    public static DArray<Float> conv2d(DArray<Float> in, DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        if (in.rank() > 4) {
            throw new IllegalArgumentException(String.format(
                    "Input must have at most 4 dimensions, but it has %d.", in.rank()));
        }
        while (in.rank() < 4) {
            in = in.stretch(0);
        }
        while (kernel.rank() < 4) {
            kernel = kernel.stretch(0);
        }

        var input = in;
        var kk = kernel;

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inH = input.dim(2);
        int inW = input.dim(3);
        int outChannels = kernel.dim(0);
        int inDepth = kernel.dim(1);
        int kH = kernel.dim(2);
        int kW = kernel.dim(3);

        if (inDepth * groups != inChannels) {
            throw new IllegalArgumentException(
                    String.format("inDepth x groups (%d x %d) must equal inChannels (%d).", inDepth, groups, inChannels));
        }
        int outDepth = outChannels / groups;
        int outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Float> output = in.dm().zeros(DType.FLOAT, Shape.of(n, outChannels, outH, outW));

        try (ExecutorService executor = Executors.newFixedThreadPool(input.dm().cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(n);
            for (int batch = 0; batch < n; batch++) {
                int b = batch;
                executor.submit(() -> {
                    DArray<Float> inBatch = input.selsq(0, b);   // (inChannels, inH, inW)
                    DArray<Float> outBatch = output.selsq(0, b); // (outChannels, outH, outW)

                    var inSlices = inBatch.chunk(0, true, inDepth);
                    var outSlices = outBatch.chunk(0, true, outDepth);
                    var kernelSlices = kk.chunk(0, true, outDepth);
                    for (int group = 0; group < groups; group++) {

                        // im2col: (inDepth * kH * kW, outH * outW)
                        DArray<Float> col = inSlices.get(group).unfold2d(kH, kW, stride, padding, dilation);
                        DArray<?> kernelSlice = kernelSlices.get(group);
                        DArray<?> kFlat = kernelSlice.reshape(Shape.of(outDepth, inDepth * kH * kW), Order.C);
                        // (outDepth, outH*outW)
                        kFlat.mm(col, outSlices.get(group).reshape(Shape.of(outDepth, outH * outW)));
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException _) {
            }
        }

        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getFloat(oc));
            }
        }
        return output;
    }

    public static DArray<Float> unfold2d(DArray<Float> in, int kH, int kW, int stride, int padding, int dilation) {
        boolean batched = in.rank() == 4;
        if (in.rank() != 3 && in.rank() != 4) {
            throw new IllegalArgumentException("Input must be a 3D or 4D array.");
        }
        DArray<Float> input = batched ? in : in.stretch(0);

        int n = input.dim(0);
        int inC = input.dim(1);

        int inH = input.dim(2);
        int inW = input.dim(3);
        int outLenH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outLenW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Float> result = input.dm().zeros(DType.FLOAT, Shape.of(n, inC, kH, kW, outLenH, outLenW));
        for (int b = 0; b < n; b++) {
            for (int c = 0; c < inC; c++) {
                for (int kh = 0; kh < kH; kh++) {
                    for (int kw = 0; kw < kW; kw++) {
                        for (int oh = 0; oh < outLenH; oh++) {
                            for (int ow = 0; ow < outLenW; ow++) {
                                int ih = oh * stride + kh * dilation - padding;
                                int iw = ow * stride + kw * dilation - padding;
                                if (ih >= 0 && ih < inH && iw >= 0 && iw < inW) {
                                    result.setFloat(input.getFloat(b, c, ih, iw), b, c, kh, kw, oh, ow);
                                }
                            }
                        }
                    }
                }
            }
        }
        var ret = result.reshape(Shape.of(n, inC * kH * kW, outLenH * outLenW));
        if (!batched) {
            ret = ret.squeeze(0);
        }
        return ret;
    }

    public static DArray<Float> convTranspose2d(DArray<Float> input, DArray<?> w, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        while (input.rank() < 4) {
            input = input.stretch(0);
        }
        while (w.rank() < 4) {
            w = w.stretch(0);
        }
        var weights = w;

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inH = input.dim(2);
        int inW = input.dim(3);
        int outDepth = weights.dim(1);
        int inDepth = inChannels / groups;
        int outChannels = outDepth * groups;
        int kH = weights.dim(2);
        int kW = weights.dim(3);

        if (input.dim(1) != weights.dim(0)) {
            throw new IllegalArgumentException("Input channels and weight output channels do not match.");
        }

        int outH = (inH - 1) * stride - 2 * padding + dilation * (kH - 1) + 1 + outputPadding;
        int outW = (inW - 1) * stride - 2 * padding + dilation * (kW - 1) + 1 + outputPadding;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, outChannels, outH, outW));

        try (ExecutorService executor = Executors.newFixedThreadPool(input.dm().cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(n);
            for (int batch = 0; batch < n; batch++) {
                DArray<?> inBatch = input.selsq(0, batch);
                DArray<?> outBatch = output.selsq(0, batch);

                executor.submit(() -> {
                    var inSlices = inBatch.chunk(0, true, inDepth);
                    var outSlices = outBatch.chunk(0, true, outDepth);
                    var kernelSlices = weights.chunk(0, true, inDepth);
                    for (int group = 0; group < groups; group++) {
                        var inSlice = inSlices.get(group);
                        var outSlice = outSlices.get(group);
                        var kernelSlice = kernelSlices.get(group);
                        for (int ih = 0; ih < inH; ih++) {
                            for (int iw = 0; iw < inW; iw++) {
                                for (int c = 0; c < inDepth; c++) {
                                    float val = inSlice.getFloat(c, ih, iw);
                                    for (int kh = 0; kh < kH; kh++) {
                                        int oh = ih * stride + kh * dilation - padding;
                                        if (oh >= 0 && oh < outH) {
                                            int ow = iw * stride - padding;
                                            for (int kw = 0; kw < kW; kw++) {
                                                if (ow >= 0 && ow < outW) {
                                                    for (int oc = 0; oc < outDepth; oc++) {
                                                        outSlice.incFloat((float) (val * kernelSlice.getFloat(c, oc, kh, kw)), oc, oh,
                                                                ow);
                                                    }
                                                }
                                                ow += dilation;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException _) {
            }
        }

        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getFloat(oc));
            }
        }
        return output;
    }

    public static DArray<Float> conv3d(DArray<Float> input, DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        while (input.rank() < 5) {
            input = input.stretch(0);
        }
        while (kernel.rank() < 5) {
            kernel = kernel.stretch(0);
        }

        int n = input.dim(0), inChannels = input.dim(1);
        int inD = input.dim(2), inH = input.dim(3), inW = input.dim(4);
        int outChannels = kernel.dim(0), inDepth = kernel.dim(1);
        int kD = kernel.dim(2), kH = kernel.dim(3), kW = kernel.dim(4);

        if (inDepth * groups != inChannels) {
            throw new IllegalArgumentException("inDepth * groups must equal inChannels.");
        }
        int outDepth = outChannels / groups;
        int outD = Math.floorDiv(inD + 2 * padding - dilation * (kD - 1) - 1, stride) + 1;
        int outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, outChannels, outD, outH, outW));
        for (int batch = 0; batch < n; batch++) {
            DArray<?> inBatch = input.selsq(0, batch);
            DArray<?> outBatch = output.selsq(0, batch);
            for (int group = 0; group < groups; group++) {
                DArray<?> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth);
                DArray<?> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth);
                DArray<?> kernelSlice = kernel.narrow(0, group * outDepth, (group + 1) * outDepth);
                DArray<?> unfold = inSlice.unfold3d(kD, kH, kW, stride, padding, dilation);
                outSlice.add_(kernelSlice.reshape(Shape.of(outDepth, inDepth * kD * kH * kW)).mm(unfold)
                        .reshape(Shape.of(outDepth, outD, outH, outW)));
            }
        }
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getFloat(oc));
            }
        }
        return output;
    }

    public static DArray<Float> unfold3d(DArray<Float> input, int kD, int kH, int kW, int stride, int padding, int dilation) {

        boolean batched = input.rank() == 5;
        if (!batched) {
            if (input.rank() != 4) {
                throw new IllegalArgumentException("Input must be a 4D or 5D array.");
            }
            input = input.stretch(0);
        }

        int n = input.dim(0);
        int inCh = input.dim(1);

        int inD = input.dim(2);
        int inH = input.dim(3);
        int inW = input.dim(4);

        int outD = Math.floorDiv(inD + 2 * padding - dilation * (kD - 1) - 1, stride) + 1;
        int outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Float> result = input.dm().zeros(DType.FLOAT, Shape.of(n, inCh, kD, kH, kW, outD, outH, outW));
        for (int b = 0; b < n; b++) {
            for (int c = 0; c < inCh; c++) {
                for (int kd = 0; kd < kD; kd++) {
                    for (int kh = 0; kh < kH; kh++) {
                        for (int kw = 0; kw < kW; kw++) {
                            for (int od = 0; od < outD; od++) {
                                for (int oh = 0; oh < outH; oh++) {
                                    for (int ow = 0; ow < outW; ow++) {
                                        int id = od * stride + kd * dilation - padding;
                                        int ih = oh * stride + kh * dilation - padding;
                                        int iw = ow * stride + kw * dilation - padding;
                                        if (id >= 0 && id < inD && ih >= 0 && ih < inH && iw >= 0 && iw < inW) {
                                            result.setFloat(input.getFloat(b, c, id, ih, iw), b, c, kd, kh, kw, od, oh, ow);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        result = result.reshape(Shape.of(n, inCh * kD * kH * kW, outD * outH * outW));
        if (!batched) {
            result = result.squeeze(0);
        }
        return result;
    }

    public static DArray<Float> convTranspose3d(DArray<Float> input, DArray<?> weights, DArray<?> bias, int stride, int padding, int dilation,
            int groups,
            int outputPadding) {
        while (input.rank() < 5) {
            input = input.stretch(0);
        }
        while (weights.rank() < 5) {
            weights = weights.stretch(0);
        }

        int n = input.dim(0), inChannels = input.dim(1);
        int inD = input.dim(2), inH = input.dim(3), inW = input.dim(4);
        int outDepth = weights.dim(1), inDepth = inChannels / groups;
        int outChannels = outDepth * groups;
        int kD = weights.dim(2), kH = weights.dim(3), kW = weights.dim(4);

        if (input.dim(1) != weights.dim(0)) {
            throw new IllegalArgumentException("Input channels and weight output channels do not match.");
        }

        int outD = (inD - 1) * stride - 2 * padding + dilation * (kD - 1) + 1 + outputPadding;
        int outH = (inH - 1) * stride - 2 * padding + dilation * (kH - 1) + 1 + outputPadding;
        int outW = (inW - 1) * stride - 2 * padding + dilation * (kW - 1) + 1 + outputPadding;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, outChannels, outD, outH, outW));
        for (int batch = 0; batch < n; batch++) {
            DArray<?> inBatch = input.selsq(0, batch);
            DArray<?> outBatch = output.selsq(0, batch);
            for (int group = 0; group < groups; group++) {
                DArray<?> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, inD, inH, inW)
                DArray<?> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth); // (outDepth, outD, outH, outW)
                DArray<?> kernelSlice = weights.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, outDepth, kD, kH, kW)

                // col = kernelFlat^T @ inSlice_flat: (outDepth*kD*kH*kW, inDepth) @ (inDepth, inD*inH*inW) = (outDepth*kD*kH*kW, inD*inH*inW)
                DArray<?> kernelFlat = kernelSlice.reshape(Shape.of(inDepth, outDepth * kD * kH * kW));
                DArray<?> col = kernelFlat.t().mm(inSlice.reshape(Shape.of(inDepth, inD * inH * inW)));

                // fold col -> outSlice
                for (int oc = 0; oc < outDepth; oc++) {
                    for (int kd = 0; kd < kD; kd++) {
                        for (int kh = 0; kh < kH; kh++) {
                            for (int kw = 0; kw < kW; kw++) {
                                int colRow = oc * kD * kH * kW + kd * kH * kW + kh * kW + kw;
                                for (int id = 0; id < inD; id++) {
                                    int od = id * stride + kd * dilation - padding;
                                    if (od >= 0 && od < outD) {
                                        for (int ih = 0; ih < inH; ih++) {
                                            int oh = ih * stride + kh * dilation - padding;
                                            if (oh >= 0 && oh < outH) {
                                                for (int iw = 0; iw < inW; iw++) {
                                                    int ow = iw * stride + kw * dilation - padding;
                                                    if (ow >= 0 && ow < outW) {
                                                        outSlice.incFloat(col.getFloat(colRow, id * inH * inW + ih * inW + iw), oc, od,
                                                                oh, ow);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getFloat(oc));
            }
        }
        return output;
    }

    public static Pair<DArray<Float>, DArray<Integer>> maxPool1d(DArray<Float> input, int kSize, int stride, int padding, int dilation, boolean ceilMode) {
        boolean batched = input.rank() == 3;
        if (input.rank() != 2 && input.rank() != 3) {
            throw new IllegalArgumentException("Input must be a 2D or 3D array.");
        }
        if (!batched) {
            input = input.stretch(0);
        }

        int n = input.dim(0);
        int channels = input.dim(1);
        int inLen = input.dim(2);

        int outLen;
        if (ceilMode) {
            outLen = (int) Math.ceil((inLen + 2.0 * padding - dilation * (kSize - 1) - 1) / stride + 1);
        } else {
            outLen = Math.floorDiv(inLen + 2 * padding - dilation * (kSize - 1) - 1, stride) + 1;
        }

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, channels, outLen));
        DArray<Integer> indices = input.dm().zeros(DType.INTEGER, Shape.of(n, channels, outLen));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < channels; c++) {
                for (int o = 0; o < outLen; o++) {
                    float maxVal = Float.NEGATIVE_INFINITY;
                    int maxIdx = -1;
                    for (int k = 0; k < kSize; k++) {
                        int inPos = o * stride + k * dilation - padding;
                        if (inPos >= 0 && inPos < inLen) {
                            float val = input.getFloat(b, c, inPos);
                            if (val > maxVal) {
                                maxVal = val;
                                maxIdx = inPos;
                            }
                        }
                    }
                    output.setFloat(maxVal, b, c, o);
                    indices.setInt(maxIdx, b, c, o);
                }
            }
        }

        if (!batched) {
            output = output.squeeze(0);
            indices = indices.squeeze(0);
        }
        return Pair.from(output, indices);
    }

    public static DArray<Float> maxUnpool1d(DArray<Float> input, DArray<Integer> indices, int kSize, int stride, int padding, int outputSize) {
        boolean batched = input.rank() == 3;
        if (input.rank() != 2 && input.rank() != 3) {
            throw new IllegalArgumentException("Input must be a 2D or 3D array.");
        }
        if (!batched) {
            input = input.stretch(0);
            indices = indices.stretch(0);
        }

        int n = input.dim(0);
        int channels = input.dim(1);
        int inLen = input.dim(2);

        int outLen = outputSize > 0 ? outputSize : (inLen - 1) * stride - 2 * padding + kSize;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, channels, outLen));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < channels; c++) {
                for (int i = 0; i < inLen; i++) {
                    int idx = indices.getInt(b, c, i);
                    if (idx >= 0 && idx < outLen) {
                        output.setFloat(input.getFloat(b, c, i), b, c, idx);
                    }
                }
            }
        }

        if (!batched) {
            output = output.squeeze(0);
        }
        return output;
    }

    public static Pair<DArray<Float>, DArray<Integer>> maxPool2d(DArray<Float> input, int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        boolean batched = input.rank() == 4;
        if (input.rank() != 3 && input.rank() != 4) {
            throw new IllegalArgumentException("Input must be a 3D or 4D array.");
        }
        if (!batched) {
            input = input.stretch(0);
        }

        int n = input.dim(0);
        int channels = input.dim(1);
        int inH = input.dim(2);
        int inW = input.dim(3);

        int outH, outW;
        if (ceilMode) {
            outH = (int) Math.ceil((inH + 2.0 * padding - dilation * (kH - 1) - 1) / stride + 1);
            outW = (int) Math.ceil((inW + 2.0 * padding - dilation * (kW - 1) - 1) / stride + 1);
        } else {
            outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
            outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;
        }

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, channels, outH, outW));
        DArray<Integer> indices = input.dm().zeros(DType.INTEGER, Shape.of(n, channels, outH, outW));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < channels; c++) {
                for (int oh = 0; oh < outH; oh++) {
                    for (int ow = 0; ow < outW; ow++) {
                        float maxVal = Float.NEGATIVE_INFINITY;
                        int maxIdx = -1;
                        for (int kh = 0; kh < kH; kh++) {
                            int ih = oh * stride + kh * dilation - padding;
                            if (ih >= 0 && ih < inH) {
                                for (int kw = 0; kw < kW; kw++) {
                                    int iw = ow * stride + kw * dilation - padding;
                                    if (iw >= 0 && iw < inW) {
                                        float val = input.getFloat(b, c, ih, iw);
                                        if (val > maxVal) {
                                            maxVal = val;
                                            maxIdx = ih * inW + iw;
                                        }
                                    }
                                }
                            }
                        }
                        output.setFloat(maxVal, b, c, oh, ow);
                        indices.setInt(maxIdx, b, c, oh, ow);
                    }
                }
            }
        }

        if (!batched) {
            output = output.squeeze(0);
            indices = indices.squeeze(0);
        }
        return Pair.from(output, indices);
    }

    public static DArray<Float> maxUnpool2d(DArray<Float> input, DArray<Integer> indices, int kH, int kW, int stride, int padding, int outH, int outW) {
        boolean batched = input.rank() == 4;
        if (input.rank() != 3 && input.rank() != 4) {
            throw new IllegalArgumentException("Input must be a 3D or 4D array.");
        }
        if (!batched) {
            input = input.stretch(0);
            indices = indices.stretch(0);
        }

        int n = input.dim(0);
        int channels = input.dim(1);
        int inH = input.dim(2);
        int inW = input.dim(3);

        int outputH = outH > 0 ? outH : (inH - 1) * stride - 2 * padding + kH;
        int outputW = outW > 0 ? outW : (inW - 1) * stride - 2 * padding + kW;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, channels, outputH, outputW));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < channels; c++) {
                for (int ih = 0; ih < inH; ih++) {
                    for (int iw = 0; iw < inW; iw++) {
                        int idx = indices.getInt(b, c, ih, iw);
                        if (idx >= 0) {
                            int oh = idx / outputW;
                            int ow = idx % outputW;
                            if (oh < outputH && ow < outputW) {
                                output.setFloat(input.getFloat(b, c, ih, iw), b, c, oh, ow);
                            }
                        }
                    }
                }
            }
        }

        if (!batched) {
            output = output.squeeze(0);
        }
        return output;
    }

    public static Pair<DArray<Float>, DArray<Integer>> maxPool3d(DArray<Float> input, int kD, int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        boolean batched = input.rank() == 5;
        if (input.rank() != 4 && input.rank() != 5) {
            throw new IllegalArgumentException("Input must be a 4D or 5D array.");
        }
        if (!batched) {
            input = input.stretch(0);
        }

        int n = input.dim(0);
        int channels = input.dim(1);
        int inD = input.dim(2);
        int inH = input.dim(3);
        int inW = input.dim(4);

        int outD, outH, outW;
        if (ceilMode) {
            outD = (int) Math.ceil((inD + 2.0 * padding - dilation * (kD - 1) - 1) / stride + 1);
            outH = (int) Math.ceil((inH + 2.0 * padding - dilation * (kH - 1) - 1) / stride + 1);
            outW = (int) Math.ceil((inW + 2.0 * padding - dilation * (kW - 1) - 1) / stride + 1);
        } else {
            outD = Math.floorDiv(inD + 2 * padding - dilation * (kD - 1) - 1, stride) + 1;
            outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
            outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;
        }

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, channels, outD, outH, outW));
        DArray<Integer> indices = input.dm().zeros(DType.INTEGER, Shape.of(n, channels, outD, outH, outW));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < channels; c++) {
                for (int od = 0; od < outD; od++) {
                    for (int oh = 0; oh < outH; oh++) {
                        for (int ow = 0; ow < outW; ow++) {
                            float maxVal = Float.NEGATIVE_INFINITY;
                            int maxIdx = -1;
                            for (int kd = 0; kd < kD; kd++) {
                                int id = od * stride + kd * dilation - padding;
                                if (id >= 0 && id < inD) {
                                    for (int kh = 0; kh < kH; kh++) {
                                        int ih = oh * stride + kh * dilation - padding;
                                        if (ih >= 0 && ih < inH) {
                                            for (int kw = 0; kw < kW; kw++) {
                                                int iw = ow * stride + kw * dilation - padding;
                                                if (iw >= 0 && iw < inW) {
                                                    float val = input.getFloat(b, c, id, ih, iw);
                                                    if (val > maxVal) {
                                                        maxVal = val;
                                                        maxIdx = id * inH * inW + ih * inW + iw;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            output.setFloat(maxVal, b, c, od, oh, ow);
                            indices.setInt(maxIdx, b, c, od, oh, ow);
                        }
                    }
                }
            }
        }

        if (!batched) {
            output = output.squeeze(0);
            indices = indices.squeeze(0);
        }
        return Pair.from(output, indices);
    }

    public static DArray<Float> maxUnpool3d(DArray<Float> input, DArray<Integer> indices, int kD, int kH, int kW, int stride, int padding, int outD, int outH, int outW) {
        boolean batched = input.rank() == 5;
        if (input.rank() != 4 && input.rank() != 5) {
            throw new IllegalArgumentException("Input must be a 4D or 5D array.");
        }
        if (!batched) {
            input = input.stretch(0);
            indices = indices.stretch(0);
        }

        int n = input.dim(0);
        int channels = input.dim(1);
        int inD = input.dim(2);
        int inH = input.dim(3);
        int inW = input.dim(4);

        int outputD = outD > 0 ? outD : (inD - 1) * stride - 2 * padding + kD;
        int outputH = outH > 0 ? outH : (inH - 1) * stride - 2 * padding + kH;
        int outputW = outW > 0 ? outW : (inW - 1) * stride - 2 * padding + kW;

        DArray<Float> output = input.dm().zeros(DType.FLOAT, Shape.of(n, channels, outputD, outputH, outputW));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < channels; c++) {
                for (int id = 0; id < inD; id++) {
                    for (int ih = 0; ih < inH; ih++) {
                        for (int iw = 0; iw < inW; iw++) {
                            int idx = indices.getInt(b, c, id, ih, iw);
                            if (idx >= 0) {
                                int od = idx / (outputH * outputW);
                                int oh = (idx % (outputH * outputW)) / outputW;
                                int ow = idx % outputW;
                                if (od < outputD && oh < outputH && ow < outputW) {
                                    output.setFloat(input.getFloat(b, c, id, ih, iw), b, c, od, oh, ow);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!batched) {
            output = output.squeeze(0);
        }
        return output;
    }
}
