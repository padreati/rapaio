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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class ST4 {

    private static final String st4dir = "/rapaio-code-gen/src/rapaio/codegen/st/unary_op.stg";

    public static void processTemplates(String root) throws IOException {
        processUnaryOpTemplates(root);
    }

    public static void processUnaryOpTemplates(String root) throws IOException {
        STGroup stg = new STGroupFile(root + st4dir);
        for (UnaryOpParam op : DArrayOperations.unaryOperations()) {
            ST st = stg.getInstanceOf("unary_op");
            st.add("op", op);
            String text = st.render();

            String fileName = root + "/rapaio-lib/src/rapaio/darray/operator/unary/" + op.name + ".java";
            try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
                w.write(text);
                w.flush();
            }
        }
    }

    public static class UnaryOpParam {

        public String name;
        public boolean floatingPointOnly;
        public String byteValueOp;
        public String byteVectorOp;
        public String intValueOp;
        public String intVectorOp;
        public String floatValueOp;
        public String floatVectorOp;
        public String doubleValueOp;
        public String doubleVectorOp;

        public UnaryOpParam name(String name) {
            this.name = name;
            return this;
        }

        public UnaryOpParam floatingPointOnly(boolean floatingPointOnly) {
            this.floatingPointOnly = floatingPointOnly;
            return this;
        }

        public UnaryOpParam byteValueOp(String byteValueOp) {
            this.byteValueOp = byteValueOp;
            return this;
        }

        public UnaryOpParam byteVectorOp(String byteVectorOp) {
            this.byteVectorOp = byteVectorOp;
            return this;
        }

        public UnaryOpParam intValueOp(String intValueOp) {
            this.intValueOp = intValueOp;
            return this;
        }

        public UnaryOpParam intVectorOp(String intVectorOp) {
            this.intVectorOp = intVectorOp;
            return this;
        }

        public UnaryOpParam floatValueOp(String floatValueOp) {
            this.floatValueOp = floatValueOp;
            return this;
        }

        public UnaryOpParam floatVectorOp(String floatVectorOp) {
            this.floatVectorOp = floatVectorOp;
            return this;
        }

        public UnaryOpParam doubleValueOp(String doubleValueOp) {
            this.doubleValueOp = doubleValueOp;
            return this;
        }

        public UnaryOpParam doubleVectorOp(String doubleVectorOp) {
            this.doubleVectorOp = doubleVectorOp;
            return this;
        }
    }

}