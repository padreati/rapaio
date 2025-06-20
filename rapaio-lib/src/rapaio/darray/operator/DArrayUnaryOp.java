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

package rapaio.darray.operator;

import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.data.OperationNotAvailableException;

public abstract class DArrayUnaryOp {

    private final boolean floatingPointOnly;

    public DArrayUnaryOp(boolean floatingPointOnly) {
        this.floatingPointOnly = floatingPointOnly;
    }

    public final boolean floatingPointOnly() {
        return floatingPointOnly;
    }

    public final void applyByte(StrideLoopDescriptor loop, Storage s) {
        if (floatingPointOnly()) {
            throw new OperationNotAvailableException();
        }
        if (s.supportSimd()) {
            if (loop.step == 1) {
                applyUnitByte(loop, s);
            } else {
                applyStepByte(loop, s);
            }
        } else {
            applyGenericByte(loop, s);
        }
    }

    public final void applyInt(StrideLoopDescriptor loop, Storage s) {
        if (floatingPointOnly()) {
            throw new OperationNotAvailableException();
        }
        if (s.supportSimd()) {
            if (loop.step == 1) {
                applyUnitInt(loop, s);
            } else {
                applyStepInt(loop, s);
            }
        } else {
            applyGenericInt(loop, s);
        }
    }

    public final void applyFloat(StrideLoopDescriptor loop, Storage s) {
        if (s.supportSimd()) {
            if (loop.step == 1) {
                applyUnitFloat(loop, s);
            } else {
                applyStepFloat(loop, s);
            }
        } else {
            applyGenericFloat(loop, s);
        }
    }

    public final void applyDouble(StrideLoopDescriptor loop, Storage s) {
        if (s.supportSimd()) {
            if (loop.step == 1) {
                applyUnitDouble(loop, s);
            } else {
                applyStepDouble(loop, s);
            }
        } else {
            applyGenericDouble(loop, s);
        }
    }

    protected abstract void applyUnitByte(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyStepByte(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyGenericByte(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyUnitInt(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyStepInt(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyGenericInt(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyUnitFloat(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyStepFloat(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyGenericFloat(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyUnitDouble(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyStepDouble(StrideLoopDescriptor loop, Storage s);

    protected abstract void applyGenericDouble(StrideLoopDescriptor loop, Storage s);
}

