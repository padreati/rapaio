/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.util;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/28/15.
 */
public class Triple<T1, T2, T3> implements Serializable {

    private static final long serialVersionUID = -8902156268652553858L;

    public T1 _1;
    public T2 _2;
    public T3 _3;

    private Triple(T1 _1, T2 _2, T3 _3) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    public static <U1, U2, U3> Triple<U1, U2, U3> from(U1 u1, U2 u2, U3 u3) {
        return new Triple<>(u1, u2, u3);
    }

    @Override
    public String toString() {
        return "Triple{_1=" + _1 + ", _2=" + _2 + ", _3=" + _3 + "}";
    }
}
