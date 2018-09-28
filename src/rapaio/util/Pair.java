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
 * Tuple of length 2.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Pair<T1, T2> implements Serializable {

    private static final long serialVersionUID = -1594916059995575867L;

    public T1 _1;
    public T2 _2;

    protected Pair(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public static <U, V> Pair<U, V> from(U u, V v) {
        return new Pair<>(u, v);
    }

    @Override
    public String toString() {
        return "Pair{" + _1 + ", " + _2 + "}";
    }


    public void update(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public void update(Pair<T1, T2> p) {
    	update(_1, _2);
    }
}
