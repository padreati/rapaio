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
 */

package rapaio.experiment.json;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/20/15.
 */
public interface JsonParser {

    char LEFT_SQUARE = '[';
    char RIGHT_SQUARE = ']';
    char LEFT_CURLY = '{';
    char RIGHT_CURLY = '}';
    char COLON = ':';
    char COMMA = ',';
    String KEY_TRUE = "true";
    String KEY_FALSE = "false";
    String KEY_NULL = "null";
    char[] WHITE_CHARS = new char[]{' ', '\t', '\n', '\r'};

    default boolean isNumeric(int next) {
        switch ((char) next) {
            case '+':
            case '-':
            case '.':
            case 'e':
            case 'E':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
        }
        return false;
    }

    default boolean isWhite(int ch) {
        for (char w : WHITE_CHARS)
            if (ch == w)
                return true;
        return false;
    }

}
