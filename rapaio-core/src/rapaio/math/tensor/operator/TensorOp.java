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

import rapaio.math.tensor.DType;
import rapaio.math.tensor.operator.impl.AbsOperator;
import rapaio.math.tensor.operator.impl.AcosOperator;
import rapaio.math.tensor.operator.impl.AddAssocOperator;
import rapaio.math.tensor.operator.impl.AddOperator;
import rapaio.math.tensor.operator.impl.AsinOperator;
import rapaio.math.tensor.operator.impl.AtanOperator;
import rapaio.math.tensor.operator.impl.CeilOperator;
import rapaio.math.tensor.operator.impl.ClampOperator;
import rapaio.math.tensor.operator.impl.CosOperator;
import rapaio.math.tensor.operator.impl.CoshOperator;
import rapaio.math.tensor.operator.impl.DivOperator;
import rapaio.math.tensor.operator.impl.ExpOperator;
import rapaio.math.tensor.operator.impl.Expm1Operator;
import rapaio.math.tensor.operator.impl.FloorOperator;
import rapaio.math.tensor.operator.impl.LogOperator;
import rapaio.math.tensor.operator.impl.Log1pOperator;
import rapaio.math.tensor.operator.impl.MaxAssocOperator;
import rapaio.math.tensor.operator.impl.MaxOperator;
import rapaio.math.tensor.operator.impl.MinAssocOperator;
import rapaio.math.tensor.operator.impl.MinOperator;
import rapaio.math.tensor.operator.impl.MulAssocOperator;
import rapaio.math.tensor.operator.impl.MulOperator;
import rapaio.math.tensor.operator.impl.NegOperator;
import rapaio.math.tensor.operator.impl.RintOperator;
import rapaio.math.tensor.operator.impl.SinOperator;
import rapaio.math.tensor.operator.impl.SinhOperator;
import rapaio.math.tensor.operator.impl.SqrOperator;
import rapaio.math.tensor.operator.impl.SqrtOperator;
import rapaio.math.tensor.operator.impl.SubOperator;
import rapaio.math.tensor.operator.impl.TanOperator;
import rapaio.math.tensor.operator.impl.TanhOperator;

public final class TensorOp {

    private static final AbsOperator ABS = new AbsOperator();
    private static final NegOperator NEG = new NegOperator();

    private static final ExpOperator EXP = new ExpOperator();
    private static final Expm1Operator EXPM1 = new Expm1Operator();
    private static final LogOperator LOG = new LogOperator();
    private static final Log1pOperator LOG1P = new Log1pOperator();

    private static final CeilOperator CEIL = new CeilOperator();
    private static final FloorOperator FLOOR = new FloorOperator();
    private static final RintOperator RINT = new RintOperator();

    private static final SinOperator SIN = new SinOperator();
    private static final AsinOperator ASIN = new AsinOperator();
    private static final SinhOperator SINH = new SinhOperator();
    private static final CosOperator COS = new CosOperator();
    private static final AcosOperator ACOS = new AcosOperator();
    private static final CoshOperator COSH = new CoshOperator();
    private static final TanOperator TAN = new TanOperator();
    private static final AtanOperator ATAN = new AtanOperator();
    private static final TanhOperator TANH = new TanhOperator();

    private static final SqrOperator SQR = new SqrOperator();
    private static final SqrtOperator SQRT = new SqrtOperator();

    private static final AddOperator ADD = new AddOperator();
    private static final SubOperator SUB = new SubOperator();
    private static final MulOperator MUL = new MulOperator();
    private static final DivOperator DIV = new DivOperator();
    private static final MinOperator MIN = new MinOperator();
    private static final MaxOperator MAX = new MaxOperator();

    private static final AddAssocOperator ADD_ASSOC = new AddAssocOperator();
    private static final MulAssocOperator MUL_ASSOC = new MulAssocOperator();
    private static final MinAssocOperator MIN_ASSOC = new MinAssocOperator();
    private static final MaxAssocOperator MAX_ASSOC = new MaxAssocOperator();


    public static RintOperator rint() {
        return RINT;
    }

    public static CeilOperator ceil() {
        return CEIL;
    }

    public static FloorOperator floor() {
        return FLOOR;
    }


    public static AbsOperator abs() {
        return ABS;
    }

    public static NegOperator neg() {
        return NEG;
    }

    public static LogOperator log() {
        return LOG;
    }

    public static Log1pOperator log1p() {
        return LOG1P;
    }

    public static ExpOperator exp() {
        return EXP;
    }

    public static Expm1Operator expm1() {
        return EXPM1;
    }

    public static SinOperator sin() {
        return SIN;
    }

    public static AsinOperator asin() {
        return ASIN;
    }

    public static SinhOperator sinh() {
        return SINH;
    }

    public static CosOperator cos() {
        return COS;
    }

    public static AcosOperator acos() {
        return ACOS;
    }

    public static CoshOperator cosh() {
        return COSH;
    }

    public static TanOperator tan() {
        return TAN;
    }

    public static AtanOperator atan() {
        return ATAN;
    }

    public static TanhOperator tanh() {
        return TANH;
    }


    public static SqrOperator sqr() {
        return SQR;
    }

    public static SqrtOperator sqrt() {
        return SQRT;
    }


    public static AddOperator add() {
        return ADD;
    }

    public static SubOperator sub() {
        return SUB;
    }

    public static MulOperator mul() {
        return MUL;
    }

    public static DivOperator div() {
        return DIV;
    }

    public static MinOperator min() {
        return MIN;
    }

    public static MaxOperator max() {
        return MAX;
    }

    public static AddAssocOperator addAssoc() {
        return ADD_ASSOC;
    }

    public static MulAssocOperator mulAssoc() {
        return MUL_ASSOC;
    }

    public static MinAssocOperator minAssoc() {
        return MIN_ASSOC;
    }

    public static MaxAssocOperator maxAssoc() {
        return MAX_ASSOC;
    }

    public static <N extends Number> ClampOperator<N> clamp(DType<N> dtype, N min, N max) {
        return new ClampOperator<>(dtype, min, max);
    }
}
