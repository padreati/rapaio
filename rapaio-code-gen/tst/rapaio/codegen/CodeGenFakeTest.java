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
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class CodeGenFakeTest {

    /**
     * This test is not intended to test something in codegen.
     * <p>
     * The intention is to start source generation process so that the dependent projects does not have to rely on codegen module.
     *
     * @throws IOException
     */
    @Test
    public void doChanges() throws IOException {


        Path path = Path.of(".").toAbsolutePath().getParent().getParent().resolve("rapaio-lib").resolve("src");
        CodeGenApp.main(new String[] {path.toAbsolutePath().toString() + "/"});
    }
}
