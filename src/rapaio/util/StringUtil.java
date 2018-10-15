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

/**
 * Various utility tools for string processing
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/18.
 */
public final class StringUtil {

    /**
     * Builds a string of a given length filled with the same character
     *
     * @param len length of the string
     * @param ch fill character
     * @return string instance
     */
    public static String repeat(int len, char ch) {
        char[] lineChars = new char[len];
        for (int i = 0; i < len; i++) {
            lineChars[i] = ch;
        }
        return String.valueOf(lineChars);
    }

}
