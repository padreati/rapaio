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

package rapaio.codegen.st;

import java.util.List;

public class DArrayOperations {

    public static List<UnaryOpParam> unaryOperations() {
        return List.of(
                new UnaryOpParam()
                        .name("UnaryOpAbs")
                        .floatingPointOnly(false)
                        .byteValueOp("a = (byte) Math.abs(a);")
                        .byteVectorOp("a = a.abs();")
                        .intValueOp("a = Math.abs(a);")
                        .intVectorOp("a = a.abs();")
                        .floatValueOp("a = Math.abs(a);")
                        .floatVectorOp("a = a.abs();")
                        .doubleValueOp("a = Math.abs(a);")
                        .doubleVectorOp("a = a.abs();"),
                new UnaryOpParam()
                        .name("UnaryOpSqr")
                        .floatingPointOnly(false)
                        .byteValueOp("a = (byte) (a * a);")
                        .byteVectorOp("a = a.mul(a);")
                        .intValueOp("a = a * a;")
                        .intVectorOp("a = a.mul(a);")
                        .floatValueOp("a = a * a;")
                        .floatVectorOp("a = a.mul(a);")
                        .doubleValueOp("a = a * a;")
                        .doubleVectorOp("a = a.mul(a);"),
                new UnaryOpParam()
                        .name("UnaryOpSqrt")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.sqrt(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.SQRT);")
                        .doubleValueOp("a = Math.sqrt(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.SQRT);"),
                new UnaryOpParam()
                        .name("UnaryOpAcos")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.acos(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.ACOS);")
                        .doubleValueOp("a = Math.acos(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.ACOS);"),
                new UnaryOpParam()
                        .name("UnaryOpAsin")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.asin(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.ASIN);")
                        .doubleValueOp("a = Math.asin(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.ASIN);"),
                new UnaryOpParam()
                        .name("UnaryOpCos")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.cos(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.COS);")
                        .doubleValueOp("a = Math.cos(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.COS);"),
                new UnaryOpParam()
                        .name("UnaryOpCosh")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.cosh(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.COSH);")
                        .doubleValueOp("a = Math.cosh(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.COSH);"),
                new UnaryOpParam()
                        .name("UnaryOpSin")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.sin(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.SIN);")
                        .doubleValueOp("a = Math.sin(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.SIN);"),
                new UnaryOpParam()
                        .name("UnaryOpSinh")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.sinh(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.SINH);")
                        .doubleValueOp("a = Math.sinh(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.SINH);"),
                new UnaryOpParam()
                        .name("UnaryOpTan")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.tan(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.TAN);")
                        .doubleValueOp("a = Math.tan(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.TAN);"),
                new UnaryOpParam()
                        .name("UnaryOpTanh")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.tanh(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.TANH);")
                        .doubleValueOp("a = Math.tanh(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.TANH);"),
                new UnaryOpParam()
                        .name("UnaryOpAtan")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.atan(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.ATAN);")
                        .doubleValueOp("a = Math.atan(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.ATAN);"),
                new UnaryOpParam()
                        .name("UnaryOpExp")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.exp(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.EXP);")
                        .doubleValueOp("a = Math.exp(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.EXP);"),
                new UnaryOpParam()
                        .name("UnaryOpExpm1")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.expm1(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.EXPM1);")
                        .doubleValueOp("a = Math.expm1(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.EXPM1);"),
                new UnaryOpParam()
                        .name("UnaryOpLog")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.log(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.LOG);")
                        .doubleValueOp("a = Math.log(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.LOG);"),
                new UnaryOpParam()
                        .name("UnaryOpLog1p")
                        .floatingPointOnly(true)
                        .floatValueOp("a = (float) Math.log1p(a);")
                        .floatVectorOp("a = a.lanewise(VectorOperators.LOG1P);")
                        .doubleValueOp("a = Math.log1p(a);")
                        .doubleVectorOp("a = a.lanewise(VectorOperators.LOG1P);"),
                new UnaryOpParam()
                        .name("UnaryOpNeg")
                        .floatingPointOnly(false)
                        .byteValueOp("a = (byte) (-a);")
                        .byteVectorOp("a = a.neg();")
                        .intValueOp("a = -a;")
                        .intVectorOp("a = a.neg();")
                        .floatValueOp("a = -a;")
                        .floatVectorOp("a = a.neg();")
                        .doubleValueOp("a = -a;")
                        .doubleVectorOp("a = a.neg();")

        );
    }
}
