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

package rapaio.math.tensor;

import rapaio.math.tensor.operators.TensorOpACos;
import rapaio.math.tensor.operators.TensorOpASin;
import rapaio.math.tensor.operators.TensorOpATan;
import rapaio.math.tensor.operators.TensorOpAbs;
import rapaio.math.tensor.operators.TensorOpCos;
import rapaio.math.tensor.operators.TensorOpCosh;
import rapaio.math.tensor.operators.TensorOpExp;
import rapaio.math.tensor.operators.TensorOpExpm1;
import rapaio.math.tensor.operators.TensorOpLog;
import rapaio.math.tensor.operators.TensorOpLog1p;
import rapaio.math.tensor.operators.TensorOpNeg;
import rapaio.math.tensor.operators.TensorOpSin;
import rapaio.math.tensor.operators.TensorOpSinh;
import rapaio.math.tensor.operators.TensorOpTan;
import rapaio.math.tensor.operators.TensorOpTanh;

public final class TensorOps {

    public static final TensorOpAbs ABS = new TensorOpAbs();
    public static final TensorOpNeg NEG = new TensorOpNeg();

    public static final TensorOpLog LOG = new TensorOpLog();
    public static final TensorOpLog1p LOG1P = new TensorOpLog1p();

    public static final TensorOpExp EXP = new TensorOpExp();
    public static final TensorOpExpm1 EXPM1 = new TensorOpExpm1();

    public static final TensorOpSin SIN = new TensorOpSin();
    public static final TensorOpASin ASIN = new TensorOpASin();
    public static final TensorOpSinh SINH = new TensorOpSinh();
    public static final TensorOpCos COS = new TensorOpCos();
    public static final TensorOpACos ACOS = new TensorOpACos();
    public static final TensorOpCosh COSH = new TensorOpCosh();
    public static final TensorOpTan TAN = new TensorOpTan();
    public static final TensorOpATan ATAN = new TensorOpATan();
    public static final TensorOpTanh TANH = new TensorOpTanh();
}
