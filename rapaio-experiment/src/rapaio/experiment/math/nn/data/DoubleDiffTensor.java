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

package rapaio.experiment.math.nn.data;

import java.util.List;
import java.util.function.Function;

import rapaio.experiment.math.nn.DiffTensor;
import rapaio.experiment.math.nn.gradient.GradientTape;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;

public final class DoubleDiffTensor extends AbstractDiffTensor {

    public static DoubleDiffTensor of(Tensor<Double> tensor, GradientTape tape) {
        return new DoubleDiffTensor(null, tensor, tape);
    }

    public static DoubleDiffTensor of(String name, Tensor<Double> tensor, GradientTape tape) {
        return new DoubleDiffTensor(name, tensor, tape);
    }

    public static DoubleDiffTensor ofAny(String name, Object object, GradientTape tape) {
        if (object instanceof Tensor<?> tensor && tensor.dtype() == DType.DOUBLE) {
            return new DoubleDiffTensor(name, (Tensor<Double>) tensor, tape);
        }
        throw new IllegalArgumentException();
    }

    private final Tensor<Double> tensor;

    private DoubleDiffTensor(String name, Tensor<Double> tensor, GradientTape tape) {
        super(name, tape);
        this.tensor = tensor;
    }

    @Override
    public DType<?> dtype() {
        return DType.DOUBLE;
    }

    @Override
    public Tensor<Double> asDouble() {
        return tensor;
    }

    @Override
    public DiffTensor mul(DiffTensor t, GradientTape tape) {
        DiffTensor r = DoubleDiffTensor.of(this.tensor.mul(t.asDouble()), tape);
        System.out.printf("%s = %s * %s%n", r.name(), this.name, t.name());

        List<String> inputs = List.of(this.name(), t.name());
        List<String> outputs = List.of(r.name());

        // define backprop
        Function<List<DiffTensor>, List<DiffTensor>> propagate = (dL_doutputs) -> {
            var dL_dr = dL_doutputs.getFirst();

            var dr_dself = t; // partial derivative of r = self * rhs
            var dr_drhs = this; // partial derivative of r = self * rhs

            //chain rule propagation from outputs to inputs of multiply
            if (dL_dr.asDouble().shape().equals(Shape.of())) {
                return List.of(dr_dself, dr_drhs);
            }
            return List.of(
                    DoubleDiffTensor.of(dL_dr.asDouble().mul(dr_dself.asDouble()), tape),
                    DoubleDiffTensor.of(dL_dr.asDouble().mul(dr_drhs.asDouble()), tape)
            );
        };
        // finally,we record the compute we did on the tape
        tape.add(inputs, outputs, propagate);
        return r;
    }

    /*

# sum is used to turn our matrices into a single scalar to get a loss.
# expand is the backward of sum, so it is added to make sure our Variable
# is closed under differentiation. Both have rules similar to mul above.

def operator_sum(self: Variable, name: Optional[str]) -> 'Variable':
    r = Variable(torch.sum(self.value), name=name)
    print(f'{r.name} = {self.name}.sum()')
    def propagate(dL_doutputs: List[Variable]):
        dL_dr, = dL_doutputs
        size = self.value.size()
        return [dL_dr.expand(*size)]
    gradient_tape.append(TapeEntry(inputs=[self.name], outputs=[r.name], propagate=propagate))
    return r


def operator_expand(self: Variable, sizes: List[int]) -> 'Variable':
    assert(self.value.dim() == 0) # only works for scalars
    r = Variable(self.value.expand(sizes))
    print(f'{r.name} = {self.name}.expand({sizes})')
    def propagate(dL_doutputs: List[Variable]):
        dL_dr, = dL_doutputs
        return [dL_dr.sum()]
    gradient_tape.append(TapeEntry(inputs=[self.name], outputs=[r.name], propagate=propagate))
    return r
     */

    @Override
    public DiffTensor add(DiffTensor rhs, GradientTape tape) {
        // Add follows a similar pattern to Mul, but it doesn't end up  capturing any variables.
        var r = DoubleDiffTensor.of(tensor.add(rhs.asDouble()), tape);
        System.out.printf("%s = %s + %s%n", r.name(), this.name(), rhs.name());
        Function<List<DiffTensor>, List<DiffTensor>> propagate = dL_doutputs -> {
            var dL_dr = dL_doutputs.getFirst();
            return List.of(dL_dr, dL_dr);
        };
        tape.add(List.of(this.name(), rhs.name()), List.of(r.name()), propagate);
        return r;
    }
}
