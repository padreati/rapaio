/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeGenApp {

    private static final List<CodeGenTemplate> templates;

    static {
        templates = new ArrayList<>();

        Replace[] floatReplaces = new Replace[] {
                Replace.of("double", "float"),
                Replace.of("Double", "Float"),
                Replace.of("DoubleTensor", "FloatTensor"),
                Replace.of("DOUBLE", "FLOAT")
        };

        Replace[] intReplaces = new Replace[] {
                Replace.of("DoubleVector", "IntVector"),
                Replace.of("ofDouble", "ofInt"),
                Replace.of("setDouble", "setInt"),
                Replace.of("getDouble", "getInt"),
                Replace.of("initialVectorDouble", "initialVectorInt"),
                Replace.of("initialDouble", "initialInt"),
                Replace.of("applyDouble", "applyInt"),
                Replace.of("VectorizedDouble", "VectorizedInt"),
                Replace.of("DoubleTensor","IntTensor"),
                Replace.of("DoubleStorage", "IntStorage"),
                Replace.of("DoubleArrayStorage", "IntArrayStorage"),
                Replace.of("incDouble", "incInt"),
                Replace.of("ptrGetDouble", "ptrGetInt"),
                Replace.of("ptrSetDouble", "ptrSetInt"),
                Replace.of("fillDouble", "fillInt"),
                Replace.of("double", "int"),
                Replace.of("Double", "Integer"),
                Replace.of("DOUBLE", "INTEGER")
        };

        Replace[] byteReplaces = new Replace[] {
                Replace.of("double", "byte"),
                Replace.of("Double", "Byte"),
                Replace.of("DoubleTensor", "ByteTensor"),
                Replace.of("DOUBLE", "BYTE")
        };

        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/storage/array/DoubleArrayStorage.java")
                .dst.set("rapaio/math/tensor/storage/array/FloatArrayStorage.java")
                .replaces.set(floatReplaces)
        );
        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/manager/varray/VectorizedDoubleTensorStride.java")
                .dst.set("rapaio/math/tensor/manager/varray/VectorizedFloatTensorStride.java")
                .replaces.set(floatReplaces)
        );
        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/manager/barray/BaseDoubleTensorStride.java")
                .dst.set("rapaio/math/tensor/manager/barray/BaseFloatTensorStride.java")
                .replaces.set(floatReplaces)
        );

        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/storage/array/DoubleArrayStorage.java")
                .dst.set("rapaio/math/tensor/storage/array/IntArrayStorage.java")
                .replaces.set(intReplaces)
        );
        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/manager/varray/VectorizedDoubleTensorStride.java")
                .dst.set("rapaio/math/tensor/manager/varray/VectorizedIntTensorStride.java")
                .replaces.set(intReplaces)
        );
        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/manager/barray/BaseDoubleTensorStride.java")
                .dst.set("rapaio/math/tensor/manager/barray/BaseIntTensorStride.java")
                .replaces.set(intReplaces)
        );

        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/storage/array/DoubleArrayStorage.java")
                .dst.set("rapaio/math/tensor/storage/array/ByteArrayStorage.java")
                .replaces.set(byteReplaces)
        );
        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/manager/varray/VectorizedDoubleTensorStride.java")
                .dst.set("rapaio/math/tensor/manager/varray/VectorizedByteTensorStride.java")
                .replaces.set(byteReplaces)
        );
        templates.add(new CodeGenTemplate()
                .src.set("rapaio/math/tensor/manager/barray/BaseDoubleTensorStride.java")
                .dst.set("rapaio/math/tensor/manager/barray/BaseByteTensorStride.java")
                .replaces.set(byteReplaces)
        );

    }

    public static void main(String[] args) {
//        if (args.length != 1) {
//            throw new IllegalArgumentException("Pass root as parameter.");
//        }
        String root = args.length == 1 ? args[0] : "/home/ati/work/rapaio/rapaio-core/src/";
        templates.forEach(template -> {
            try {
                template.run(root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
