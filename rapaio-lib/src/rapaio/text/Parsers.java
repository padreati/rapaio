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

import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;

public final class Parsers {

    public static final Parser<Boolean> DEFAULT_VAR_BINARY_PARSER = value -> {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }
        throw new TextParserException(String.format("The value %s could not be converted to a binary value", value));
    };

    public static Parser<Integer> DEFAULT_VAR_INT_PARSER = value -> {
        if(VarNominal.MISSING_VALUE.equals(value)) {
            return VarInt.MISSING_VALUE;
        }
        return Integer.parseInt(value);
    };

    public static Parser<Long> DEFAULT_VAR_LONG_PARSER = value -> {
        if(VarNominal.MISSING_VALUE.equals(value)) {
            return VarLong.MISSING_VALUE;
        }
        return Long.parseLong(value);
    };

    public static final Parser<Instant> DEFAULT_VAR_INSTANT_PARSER = value -> {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            return null;
        }
        return Instant.parse(value);
    };

    public static final Parser<Float> DEFAULT_VAR_FLOAT_PARSER = value -> {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            return Float.NaN;
        }
        if ("Inf".equals(value)) {
            return Float.POSITIVE_INFINITY;
        }
        if ("-Inf".equals(value)) {
            return Float.NEGATIVE_INFINITY;
        }
        return Float.parseFloat(value);
    };

    public static final Parser<Double> DEFAULT_VAR_DOUBLE_PARSER = value -> {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            return Double.NaN;
        }
        if ("Inf".equals(value)) {
            return Double.POSITIVE_INFINITY;
        }
        if ("-Inf".equals(value)) {
            return Double.NEGATIVE_INFINITY;
        }
        return Double.parseDouble(value);
    };

    public static final Parser<String> DEFAULT_VAR_NOMINAL_PARSER = value -> value;

    public static final Parser<String> DEFAULT_VAR_STRING_PARSER = value -> value;
}
