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

public record CodeGenTemplate(String src, String dst, Replace[] replaces) {

    static final String START_FREEZE = "FREEZE";
    static final String END_FREEZE = "UNFREEZE";

    public void run(String root) throws IOException {

        new File(root + dst).delete();

        boolean freeze = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(root + src));
             BufferedWriter writer = new BufferedWriter(new FileWriter(root + dst))) {

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    writer.flush();
                    break;
                }
                if (line.contains(START_FREEZE)) {
                    freeze = true;
                }
                if (line.contains(END_FREEZE)) {
                    freeze = false;
                    continue;
                }

                if (freeze) {
                    writer.write(line);
                    writer.newLine();
                } else {
                    processLine(writer, line);
                }
            }
        }
    }

    private void processLine(BufferedWriter writer, String line) throws IOException {

        for (var replace : replaces) {
            line = line.replace(replace.src(), replace.dst());
        }

        writer.write(line);
        writer.newLine();
    }
}
