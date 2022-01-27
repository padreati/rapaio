/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.text.regex;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class which describes various properties of a Unicode code point.
 */
public record CodePoint(int codePoint) {

    private static final Map<Byte, String> typeNames = new HashMap<>();

    static {
        typeNames.put(Character.UNASSIGNED, "UNASSIGNED");
        typeNames.put(Character.UPPERCASE_LETTER, "UPPERCASE_LETTER");
        typeNames.put(Character.LOWERCASE_LETTER, "LOWERCASE_LETTER");
        typeNames.put(Character.TITLECASE_LETTER, "TITLECASE_LETTER");
        typeNames.put(Character.MODIFIER_LETTER, "MODIFIER_LETTER");
        typeNames.put(Character.OTHER_LETTER, "OTHER_LETTER");
        typeNames.put(Character.NON_SPACING_MARK, "NON_SPACING_MARK");
        typeNames.put(Character.ENCLOSING_MARK, "ENCLOSING_MARK");
        typeNames.put(Character.COMBINING_SPACING_MARK, "COMBINING_SPACING_MARK");
        typeNames.put(Character.DECIMAL_DIGIT_NUMBER, "DECIMAL_DIGIT_NUMBER");
        typeNames.put(Character.LETTER_NUMBER, "LETTER_NUMBER");
        typeNames.put(Character.OTHER_NUMBER, "OTHER_NUMBER");
        typeNames.put(Character.SPACE_SEPARATOR, "SPACE_SEPARATOR");
        typeNames.put(Character.LINE_SEPARATOR, "LINE_SEPARATOR");
        typeNames.put(Character.PARAGRAPH_SEPARATOR, "PARAGRAPH_SEPARATOR");
        typeNames.put(Character.CONTROL, "CONTROL");
        typeNames.put(Character.FORMAT, "FORMAT");
        typeNames.put(Character.PRIVATE_USE, "PRIVATE_USE");
        typeNames.put(Character.SURROGATE, "SURROGATE");
        typeNames.put(Character.DASH_PUNCTUATION, "DASH_PUNCTUATION");
        typeNames.put(Character.START_PUNCTUATION, "START_PUNCTUATION");
        typeNames.put(Character.END_PUNCTUATION, "END_PUNCTUATION");
        typeNames.put(Character.CONNECTOR_PUNCTUATION, "CONNECTOR_PUNCTUATION");
        typeNames.put(Character.OTHER_PUNCTUATION, "OTHER_PUNCTUATION");
        typeNames.put(Character.MATH_SYMBOL, "MATH_SYMBOL");
        typeNames.put(Character.CURRENCY_SYMBOL, "CURRENCY_SYMBOL");
        typeNames.put(Character.MODIFIER_SYMBOL, "MODIFIER_SYMBOL");
        typeNames.put(Character.OTHER_SYMBOL, "OTHER_SYMBOL");
        typeNames.put(Character.INITIAL_QUOTE_PUNCTUATION, "INITIAL_QUOTE_PUNCTUATION");
        typeNames.put(Character.FINAL_QUOTE_PUNCTUATION, "FINAL_QUOTE_PUNCTUATION");
    }

    public String toString() {
        return new StringBuilder().appendCodePoint(codePoint).toString();
    }

    public String name() {
        return Character.getName(codePoint);
    }

    public int type() {
        return Character.getType(codePoint);
    }

    public String blockName() {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return block == null ? "null" : block.toString();
    }

    public int numericValue() {
        return Character.getNumericValue(codePoint);
    }

    public String scriptName() {
        return Character.UnicodeScript.of(codePoint).name();
    }


    public String typeName() {
        if (typeNames.containsKey((byte) type())) {
            return typeNames.get((byte) type());
        }
        return "N/A";
    }
}
