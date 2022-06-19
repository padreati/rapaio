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

package rapaio.data;

import java.io.Serializable;
import java.util.List;

/**
 * Mapping function between a set of values and a dense integer value. Dense integer
 * index values are positive values
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/20.
 */
public interface Index<T> extends Serializable {

    int size();

    boolean containsValue(T value);

    boolean containsValue(Var v, int row);

    boolean containsValue(Frame df, String varName, int row);

    int getIndex(T value);

    int getIndex(Var v, int row);

    int getIndex(Frame df, String varName, int row);

    List<Integer> getIndexList(Var v);

    List<Integer> getIndexList(Frame df, String varName);

    T getValue(int pos);

    T getValue(Var v, int row);

    T getValue(Frame df, String varName, int row);

    List<T> getValues();

    List<T> getValueList(Var v);

    List<T> getValueList(Frame df, String varName);

    String getValueString(int pos);

    List<String> getValueStrings();

}

