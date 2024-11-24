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

public interface AtomInputStream extends AutoCloseable {

    byte readByte() throws IOException;

    int readInt() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

    String readString() throws IOException;

    byte[] readBytes() throws IOException;

    int[] readInts() throws IOException;

    float[] readFloats() throws IOException;

    double[] readDoubles() throws IOException;

    <T> T loadAtom(Class<T> clazz) throws IOException;

    void close() throws IOException;
}
