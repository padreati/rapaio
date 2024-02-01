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

package commons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Utils {

    private static final String RESULT_PREFIX = "/home/ati/work/rapaio/rapaio-jmh/results/";

    public static String resultPath(Class<?> clazz) {
        return RESULT_PREFIX + clazz.getSimpleName() + "-tmp.csv";
    }

    public static String resultFinalPath(Class<?> clazz) {
        return RESULT_PREFIX + clazz.getSimpleName() + ".csv";
    }

    public static String resultPrevPath(Class<?> clazz) {
        return RESULT_PREFIX + clazz.getSimpleName() + ".prev.csv";
    }

    public static void resultPromote(Class<?> clazz) throws IOException {
        Files.deleteIfExists(Path.of(Utils.resultPrevPath(clazz)));

        if(Files.exists(Path.of(Utils.resultFinalPath(clazz)))) {
            Files.move(Path.of(Utils.resultFinalPath(clazz)), Path.of(Utils.resultPrevPath(clazz)));
        }
        if(Files.exists(Path.of(Utils.resultPath(clazz)))) {
            Files.copy(Path.of(Utils.resultPath(clazz)), Path.of(Utils.resultFinalPath(clazz)));
        }
    }
}
