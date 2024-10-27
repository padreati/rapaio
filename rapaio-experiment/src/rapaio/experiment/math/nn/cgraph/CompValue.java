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

package rapaio.experiment.math.nn.cgraph;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;
import rapaio.printer.Format;

public class CompValue {

    private boolean hasValue;
    private DType<?> dType;
    private Tensor<?> tensor;

    public CompValue() {
        this.hasValue = false;
        this.tensor = null;
    }

    public CompValue(Tensor<?> tensor) {
        this.hasValue = true;
        this.dType = tensor.dtype();
        this.tensor = tensor;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public void reset() {
        this.hasValue = false;
        this.dType = null;
        this.tensor = null;
    }

    public void assign(Tensor<?> tensor) {
        this.hasValue = true;
        this.dType = tensor.dtype();
        this.tensor = tensor;
    }

    public DType<?> dtype() {
        return dType;
    }

    public Tensor<?> tensor() {
        return tensor;
    }

    public void add_(Tensor<?> other) {
        if (!hasValue) {
            assign(other);
        } else {
            tensor.add_(other);
        }
    }

    public void sub_(Tensor<?> other) {
        if (!hasValue) {
            assign(other.neg());
        } else {
            tensor.sub_(other);
        }
    }

    @Override
    public String toString() {
        if(!hasValue) {
            return "NaN";
        }
        if(tensor.isScalar()) {
            return Format.floatFlex(tensor.getDouble());
        }
        return tensor.toString();
    }
}
