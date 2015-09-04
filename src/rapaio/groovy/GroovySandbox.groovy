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

package rapaio.groovy

import rapaio.data.VarType
import rapaio.io.Csv
import rapaio.sys.WS

import java.util.stream.Collectors

/**
 * Created by <a href="mailto:tutuianu@amazon.com">Aurelian Tutuianu</a> on 8/24/15.
 */


WS.getPrinter().setTextWidth(200)
root = new File("/home/ati/work/rapaio-kaggle/src/liberty/")
tr = new Csv().withDefaultTypes(VarType.NUMERIC, VarType.NOMINAL).read(new File(root, "train.csv")).removeVars("Id")

