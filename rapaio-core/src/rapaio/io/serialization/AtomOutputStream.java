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

package rapaio.io.serialization;

import java.io.IOException;
import java.util.List;

public interface AtomOutputStream extends AutoCloseable {

    void saveByte(byte value) throws IOException;

    void saveInt(int value) throws IOException;

    void saveFloat(float value) throws IOException;

    void saveDouble(double value) throws IOException;

    void saveString(String value) throws IOException;

    void saveBytes(byte[] bytes) throws IOException;

    void saveInts(int[] array) throws IOException;

    void saveFloats(float[] array) throws IOException;

    void putDoubles(double[] array) throws IOException;

    void saveAtom(Object atom) throws IOException;

    void saveAtoms(List<?> atoms) throws IOException;

    void close() throws IOException;
}
