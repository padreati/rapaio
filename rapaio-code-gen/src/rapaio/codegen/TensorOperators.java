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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;

public class TensorOperators {

    public static void main(String[] args) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);

        List<File> sourceFiles = collectSourceFiles();
        Iterable<? extends JavaFileObject> it = fm.getJavaFileObjects(sourceFiles.toArray(File[]::new));

        JavacTask task = (JavacTask) compiler.getTask(null, fm, null, null, null, it);
        Iterable<? extends CompilationUnitTree> units = task.parse();
        for(CompilationUnitTree unit : units) {
            for(Tree tree : unit.getTypeDecls()) {
                System.out.println(tree);
            }
        }
    }

    private static List<File> collectSourceFiles() {
        String root = GlobalProperties.defaultSourceRoot() + "rapaio/math/tensor/operator/impl";
        return collectSourceFilesRecursive(new File(root));
    }

    private static List<File> collectSourceFilesRecursive(File root) {
        List<File> files = new ArrayList<>();
        if (root.isFile()) {
            if (root.getName().endsWith(".java")) {
                files.add(root);
            }
            return files;
        }
        for(File child : Objects.requireNonNull(root.listFiles())) {
            files.addAll(collectSourceFilesRecursive(child));
        }
        return files;
    }
}

