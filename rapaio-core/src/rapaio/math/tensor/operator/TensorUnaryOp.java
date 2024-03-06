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

package rapaio.math.tensor.operator;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import rapaio.math.tensor.operator.impl.UnaryOpAbs;
import rapaio.math.tensor.operator.impl.UnaryOpAcos;
import rapaio.math.tensor.operator.impl.UnaryOpAsin;
import rapaio.math.tensor.operator.impl.UnaryOpAtan;
import rapaio.math.tensor.operator.impl.UnaryOpCeil;
import rapaio.math.tensor.operator.impl.UnaryOpCos;
import rapaio.math.tensor.operator.impl.UnaryOpCosh;
import rapaio.math.tensor.operator.impl.UnaryOpExp;
import rapaio.math.tensor.operator.impl.UnaryOpExpm1;
import rapaio.math.tensor.operator.impl.UnaryOpFloor;
import rapaio.math.tensor.operator.impl.UnaryOpLog;
import rapaio.math.tensor.operator.impl.UnaryOpLog1p;
import rapaio.math.tensor.operator.impl.UnaryOpNeg;
import rapaio.math.tensor.operator.impl.UnaryOpRint;
import rapaio.math.tensor.operator.impl.UnaryOpSin;
import rapaio.math.tensor.operator.impl.UnaryOpSinh;
import rapaio.math.tensor.operator.impl.UnaryOpSqr;
import rapaio.math.tensor.operator.impl.UnaryOpSqrt;
import rapaio.math.tensor.operator.impl.UnaryOpTan;
import rapaio.math.tensor.operator.impl.UnaryOpTanh;

public interface TensorUnaryOp {

    TensorUnaryOp RINT = new UnaryOpRint();
    TensorUnaryOp CEIL = new UnaryOpCeil();
    TensorUnaryOp FLOOR = new UnaryOpFloor();
    TensorUnaryOp ABS = new UnaryOpAbs();
    TensorUnaryOp NEG = new UnaryOpNeg();
    TensorUnaryOp LOG = new UnaryOpLog();
    TensorUnaryOp LOG1P = new UnaryOpLog1p();
    TensorUnaryOp EXP = new UnaryOpExp();
    TensorUnaryOp EXPM1 = new UnaryOpExpm1();
    TensorUnaryOp SIN = new UnaryOpSin();
    TensorUnaryOp ASIN = new UnaryOpAsin();
    TensorUnaryOp SINH = new UnaryOpSinh();
    TensorUnaryOp COS = new UnaryOpCos();
    TensorUnaryOp ACOS = new UnaryOpAcos();
    TensorUnaryOp COSH = new UnaryOpCosh();
    TensorUnaryOp TAN = new UnaryOpTan();
    TensorUnaryOp ATAN = new UnaryOpAtan();
    TensorUnaryOp TANH = new UnaryOpTanh();
    TensorUnaryOp SQR = new UnaryOpSqr();
    TensorUnaryOp SQRT = new UnaryOpSqrt();

    /**
     * @return true if vector operations are implemented, false otherwise
     */
    boolean vectorSupport();

    boolean floatingPointOnly();

    byte applyByte(byte v);

    int applyInt(int v);

    double applyDouble(double v);

    float applyFloat(float v);

    ByteVector applyByte(ByteVector v);

    IntVector applyInt(IntVector v);

    FloatVector applyFloat(FloatVector v);

    DoubleVector applyDouble(DoubleVector v);
}

