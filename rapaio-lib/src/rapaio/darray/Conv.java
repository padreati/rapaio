/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.darray;

public class Conv {

    /**
     * Applies a 1D convolution over an input signal (this tensor) composed of several input planes.
     * <p>
     * Input signal should have less or equal than 3 dimensions (N, C_in, L_in):
     * <ul>
     *     <li>N - number of batches</li>
     *     <li>C_in - number of input channels</li>
     *     <li>L_in - length of signal sequence</li>
     * </ul>
     * <p>
     * If the input signal does have less than three dimensions, the missing dimensions are the first ones.
     * If there are only two dimensions, N the number of batches in considered 1, and if there is only one dimension, N is one,
     * and C_in is considered also one.
     * <p>
     * The size of input signal, filter, and bias must agree for a 1d convolution.
     * <p>
     * The size of the resulted convolution is given by (N, C_out, L_out), where L_out
     * is computed as {@code ((L_in - 1) * inflation + 1 + 2 * padding - ((k-1) * dilation + 1)/stride + 1}.
     * <p>
     * If the value computed for {@code L_out} is not an integer value, an exception is thrown.
     *
     * @param weight    filters of shape (C_out, C_in/groups, k)
     * @param bias      bias of shape (C_out)
     * @param stride    stride of the convolved kernel
     * @param padding   implicit paddings on both sides of the input, no padding equals 0
     * @param inflation spacing between input elements, no spacing equals 1
     * @param dilation  spacing between kernel elements, no spacing equals 1
     * @param groups    split input into groups, input channels should be divisible by groups
     * @return result of the convolution
     */
    public static DArray<?> conv1d(DArray<?> input, DArray<?> weight, DArray<?> bias, int padding, int stride, int inflation, int dilation, int groups) {
        // complete input shape

        if (input.rank() > 3) {
            throw new IllegalArgumentException("Input signal (this darray) must have at most 3 dimensions.");
        }
        while (input.rank() < 3) {
            // missing dimensions means sequence of length 1, a single channel and a batch of unit size
            input = input.stretch(0);
        }

        // complete kernel shape
        DArray<?> kernel = weight;
        if (kernel.rank() > 3) {
            throw new IllegalArgumentException("Weight must have at most 3 dimensions.");
        }
        while (kernel.rank() < 3) {
            kernel = kernel.stretch(0);
        }

        int n = input.dim(0);
        int c_in = input.dim(1);
        int l_in = input.dim(2);
        int l_in_exp = (l_in - 1) * inflation + 1;

        if (c_in % groups != 0) {
            throw new IllegalArgumentException("Number of input channels must be a multiple of groups.");
        }
        if (c_in / groups != kernel.dim(0)) {
            throw new IllegalArgumentException("Number of input channels divided by groups must be equal to kernel input depth.");
        }

        int k = kernel.dim(2);
        int k_exp = (k - 1) * dilation + 1;
        int c_out_slice = kernel.dim(1);
        int c_out = c_out_slice * groups;
        int c_in_slice = c_in / groups;

        if ((l_in_exp + 2 * padding - k_exp) % stride != 0) {
            throw new IllegalArgumentException(
                    "Expanded kernel length does not cover expanded input length exactly with the given stride.");
        }

        int l_out = (l_in_exp + 2 * padding - k_exp) / stride + 1;

        DArray<?> output = input.dm().zeros(input.dt(), Shape.of(n, c_out, l_out));

        int in_start = 0;
        int out_start = 0;
        while (in_start < c_out) {
            int in_end = in_start + c_in_slice;
            int out_end = out_start + c_out_slice;
            DArray<?> cin = input.narrow(1, in_start, in_end);
            DArray<?> cout = output.narrow(1, out_start, out_end);

            // process a channel group



            in_start = in_end;
            out_start = out_end;
        }

        return output;

    }
}
