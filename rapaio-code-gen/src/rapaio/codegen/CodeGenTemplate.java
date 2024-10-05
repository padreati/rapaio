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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import rapaio.codegen.param.ListParam;
import rapaio.codegen.param.ParamSet;
import rapaio.codegen.param.ValueParam;

public class CodeGenTemplate extends ParamSet<CodeGenTemplate> {

    public ValueParam<String, CodeGenTemplate> src = new ValueParam<>(this, "", "src", s -> !s.isEmpty());
    public ValueParam<String, CodeGenTemplate> dst = new ValueParam<>(this, "", "dst", s -> !s.isEmpty());
    public ListParam<Replace, CodeGenTemplate> replaces =
            new ListParam<>(this, new ArrayList<>(), "simpleReplaces", (__, ___) -> true);

    public void run(String root) throws IOException {

        new File(root + dst.get()).delete();

        try (BufferedReader reader = new BufferedReader(new FileReader(root + src.get()))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(root + dst.get()))) {

                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        writer.flush();
                        break;
                    }

                    processLine(writer, line);
                }
            }
        }
    }

    private void processLine(BufferedWriter writer, String line) throws IOException {

        for (var replace : replaces.get()) {
            line = line.replace(replace.src(), replace.dst());
        }

        writer.write(line);
        writer.newLine();
    }
}
