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

package rapaio.experiment.math.tensor.storage;

public interface Storage<N extends Number> {

    int size();

    N get(int offset);

    void set(int offset, N v);

    void fill(int start, int len, N v);

    void add(int start, int len, N v);

    void sub(int start, int len, N v);

    void mul(int start, int len, N v);

    void div(int start, int len, N v);

    N min(int start, int len);

    int argMin(int start, int len);
}
