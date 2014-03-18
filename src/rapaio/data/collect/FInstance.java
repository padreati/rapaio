/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.collect;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public interface FInstance {

    int row();

    int rowId();

    boolean missing(int colIndex);

    boolean missing(String colName);

    void setMissing(int colIndex);

    void setMissing(String colName);

    double value(int colIndex);

    double value(String colName);

    void setValue(int colIndex, double value);

    void setValue(String colName, double value);

    int index(int colIndex);

    int index(String colName);

    void setIndex(int colIndex, int value);

    void setIndex(String colName, int value);

    String label(int colIndex);

    String label(String colName);

    void setLabel(int colIndex, String value);

    void setLabel(String colName, String value);

    String[] dictionary(int colIndex);

    String[] dictionary(String colName);
}
