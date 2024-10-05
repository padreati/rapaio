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
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.nio.file.Path;

public class CodeGen {

    private static final String file = "/home/ati/work/rapaio/rapaio-core/target/classes/rapaio/math/tensor/Tensor.class";

    public static void main(String[] args) throws IOException {

        ClassModel cm = ClassFile.of().parse(Path.of(file));
        for(var method : cm.methods()) {
            if(method.methodName().stringValue().equals("lambda$remove$0")) {
                System.out.println(method.methodName());
                if(!method.code().isPresent()) {
                    continue;
                }
                for(CodeElement el : method.code().get().elementList()) {
                    System.out.println(el);
                }
            }
        }
    }
}
