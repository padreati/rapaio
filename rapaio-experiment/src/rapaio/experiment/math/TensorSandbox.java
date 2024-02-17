/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.experiment.math;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.net.URISyntaxException;

public class TensorSandbox {
    public static void main(String[] args) throws IOException, URISyntaxException {

        int n = 100;
        MemorySegment ms = Arena.ofAuto().allocate(100*8, ValueLayout.JAVA_DOUBLE.byteAlignment());
        for (int i = 0; i < 100; i++) {
            ms.setAtIndex(ValueLayout.JAVA_DOUBLE, i, i);
        }
        print(ms, 100);

        VarHandle vh = ValueLayout.JAVA_DOUBLE.arrayElementVarHandle(100);
        vh.set(ms, 0, 0, 1000d);

        print(ms, 100);

    }

    private static void print(MemorySegment ms, int n) {
        VarHandle vh = ValueLayout.JAVA_DOUBLE.arrayElementVarHandle(n);
        for (int i = 0; i < n; i++) {
            System.out.print(vh.get(ms, 0, i) + ", ");
        }
        System.out.println();
    }
}
