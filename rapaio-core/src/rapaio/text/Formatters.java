/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.text;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;

public final class Formatters {

    public static final Formatter<Boolean> DEFAULT_VAR_BINARY_FORMATTER = value
            -> value == null ? VarNominal.MISSING_VALUE : (value ? "1" : "0");

    public static final Formatter<Integer> DEFAULT_VAR_INT_FORMATTER = value -> {
        if (value == VarInt.MISSING_VALUE)
            return VarNominal.MISSING_VALUE;
        return String.valueOf(value);
    };

    public static final Formatter<Long> DEFAULT_VAR_LONG_FORMATTER = value -> {
        if (value == VarLong.MISSING_VALUE)
            return VarNominal.MISSING_VALUE;
        return String.valueOf(value);
    };

    public static final Formatter<Instant> DEFAULT_VAR_INSTANT_FORMATTER = value
            -> {
        if(value==null){
            return VarNominal.MISSING_VALUE;
        }
        return DateTimeFormatter.ISO_INSTANT.format(value);
    };

    public static final Formatter<Float> DEFAULT_VAR_FLOAT_FORMATTER = value
            -> Float.isNaN(value) ? VarNominal.MISSING_VALUE : String.valueOf(value);

    public static final Formatter<Double> DEFAULT_VAR_DOUBLE_FORMATTER = value
            -> Double.isNaN(value) ? VarNominal.MISSING_VALUE : String.valueOf(value);

    public static final Formatter<String> DEFAULT_VAR_NOMINAL_FORMATTER = value -> value;

    public static final Formatter<String> DEFAULT_VAR_STRING_FORMATTER = value -> value;
}
