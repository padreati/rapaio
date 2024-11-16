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

package rapaio.math.narray.operator;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;

public abstract class NArrayBinaryOp {

    public abstract byte applyByte(byte a, byte b);

    public abstract ByteVector applyByte(ByteVector a, ByteVector b);

    public abstract int applyInt(int a, int b);

    public abstract IntVector applyInt(IntVector a, IntVector b);

    public abstract float applyFloat(float a, float b);

    public abstract FloatVector applyFloat(FloatVector a, FloatVector b);

    public abstract double applyDouble(double a, double b);

    public abstract DoubleVector applyDouble(DoubleVector a, DoubleVector b);
}

