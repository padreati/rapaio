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

package rapaio.experiment.sandbox

import rapaio.io.json.Json


println Arrays.stream(new File("/home/ati/ws/rapaio-amzn/data/headers6may").listFiles())
        .filter({ it.getName().startsWith("opf-") && it.getName().endsWith(".gz") })
        .filter({ it.length() > 200 }).count()


Json.stream(new File("/home/ati/ws/rapaio-amzn/data/headers6may"), {})
        .countingTop({ it.asString("").get() }).entrySet().forEach({ println(it.getValue().size()) })
