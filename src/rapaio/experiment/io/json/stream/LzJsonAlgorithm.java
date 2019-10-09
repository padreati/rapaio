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

package rapaio.experiment.io.json.stream;

import rapaio.experiment.io.json.stream.impl.*;
import rapaio.experiment.io.json.tree.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/7/15.
 */
@Deprecated
public abstract class LzJsonAlgorithm {

    protected static final byte BLOCK_STR_TERM_LIST = 0x00;
    protected static final byte BLOCK_NUM_TERM_LIST = 0x01;
    protected static final byte BLOCK_VALUE = 0x02;

    protected static final byte TYPE_NULL = 0x01;
    protected static final byte TYPE_TRUE = 0x02;
    protected static final byte TYPE_FALSE = 0x03;
    protected static final byte TYPE_NUMERIC = 0x04;
    protected static final byte TYPE_NUMERIC_TERM = 0x05;
    protected static final byte TYPE_STRING = 0x06;
    protected static final byte TYPE_STRING_TERM = 0x07;
    protected static final byte TYPE_ARRAY = 0x08;
    protected static final byte TYPE_OBJECT = 0x09;

    protected static JsonValue VALUE_NULL = new JsonNull();
    protected static JsonValue VALUE_TRUE = new JsonBool("true");
    protected static JsonValue VALUE_FALSE = new JsonBool("false");

    protected static IntEncoding encoding = IntEncoding.ENCODE_MIX;
}
