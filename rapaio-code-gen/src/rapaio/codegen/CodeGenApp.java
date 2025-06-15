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

package rapaio.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeGenApp {

    private static final String DEFAULT_ROOT = "/home/ati/work/rapaio";
    private static final List<CodeGenTemplate> templates;

    static {
        templates = new ArrayList<>();

        Replace[] floatReplaces = new Replace[] {
                Replace.of("vsDouble", "vsFloat"),
                Replace.of("double", "float"),
                Replace.of("Double", "Float"),
                Replace.of("DOUBLE", "FLOAT")
        };

        Replace[] intReplaces = new Replace[] {
                Replace.of("vsDouble", "vsInt"),
                Replace.of("Simd.zeroDouble", "Simd.zeroInt"),
                Replace.of("BaseDoubleDArrayStride", "BaseIntDArrayStride"),
                Replace.of("reduceDouble", "reduceInt"),
                Replace.of("DoubleVector", "IntVector"),
                Replace.of("ofDouble", "ofInt"),
                Replace.of("setDouble", "setInt"),
                Replace.of("getDouble", "getInt"),
                Replace.of("initialVectorDouble", "initialVectorInt"),
                Replace.of("initialDouble", "initialInt"),
                Replace.of("applyDouble", "applyInt"),
                Replace.of("VecDouble", "VecInt"),
                Replace.of("DoubleTensor","IntTensor"),
                Replace.of("DoubleStorage", "IntStorage"),
                Replace.of("DoubleArrayStorage", "IntArrayStorage"),
                Replace.of("incDouble", "incInt"),
                Replace.of("ptrGetDouble", "ptrGetInt"),
                Replace.of("ptrIncDouble", "ptrIncInt"),
                Replace.of("ptrSetDouble", "ptrSetInt"),
                Replace.of("fillDouble", "fillInt"),
                Replace.of("aggDouble","aggInt"),
                Replace.of("loadDouble","loadInt"),
                Replace.of("saveDouble","saveInt"),
                Replace.of("initDouble", "initInt"),
                Replace.of("Double2DoubleFunction", "Int2IntFunction"),
                Replace.of("opLoopDouble", "opLoopInt"),
                Replace.of("double", "int"),
                Replace.of("Double", "Integer"),
                Replace.of("DOUBLE", "INTEGER")
        };

        Replace[] byteReplaces = new Replace[] {
                Replace.of("vsDouble", "vsByte"),
                Replace.of("double", "byte"),
                Replace.of("Double", "Byte"),
                Replace.of("DOUBLE", "BYTE")
        };

        templates.add(new CodeGenTemplate()
                .src.set("rapaio/darray/manager/base/BaseDoubleDArrayStride.java")
                .dst.set("rapaio/darray/manager/base/BaseFloatDArrayStride.java")
                .replaces.set(floatReplaces)
        );

        templates.add(new CodeGenTemplate()
                .src.set("rapaio/darray/manager/base/BaseDoubleDArrayStride.java")
                .dst.set("rapaio/darray/manager/base/BaseIntDArrayStride.java")
                .replaces.set(intReplaces)
        );

        templates.add(new CodeGenTemplate()
                .src.set("rapaio/darray/manager/base/BaseDoubleDArrayStride.java")
                .dst.set("rapaio/darray/manager/base/BaseByteDArrayStride.java")
                .replaces.set(byteReplaces)
        );

    }

    public static void main(String[] args) throws IOException {

        // Simple string templates

        String root = args.length == 1 ? args[0] : DEFAULT_ROOT;
        templates.forEach(template -> {
            try {
                template.run(root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // ST4 templates

        ST4.processTemplates(root + "/../..");
    }
}
