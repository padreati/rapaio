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

package rapaio.experiment.math.nn.data;

import rapaio.experiment.math.nn.gradient.GradientTape;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;

public final class ByteDiffTensor extends AbstractDiffTensor {

    public static ByteDiffTensor of(String name, Tensor<Byte> tensor, GradientTape tape) {
        return new ByteDiffTensor(name, tensor, tape);
    }

    public static ByteDiffTensor ofAny(String name, Object object, GradientTape tape) {
        if (object instanceof Tensor<?> tensor && tensor.dtype() == DType.BYTE) {
            return new ByteDiffTensor(name, (Tensor<Byte>) tensor, tape);
        }
        throw new IllegalArgumentException();
    }

    private final Tensor<Byte> tensor;

    private ByteDiffTensor(String name, Tensor<Byte> tensor, GradientTape tape) {
        super(name, tape);
        this.tensor = tensor;
    }

    @Override
    public DType<?> dtype() {
        return DType.BYTE;
    }

    @Override
    public Tensor<Byte> asByte() {
        return tensor;
    }
}
