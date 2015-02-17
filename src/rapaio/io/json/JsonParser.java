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

package rapaio.io.json;

import java.io.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/16/15.
 */
public class JsonParser {

    private static final char LEFT_SQUARE = '[';
    private static final char RIGHT_SQUARE = ']';
    private static final char LEFT_CURLY = '{';
    private static final char RIGHT_CURLY = '}';
    private static final char COLON = ':';
    private static final char COMMA = ',';

    private static final String mkTrue = "true";
    private static final String mkFalse = "false";
    private static final String mkNull = "null";

    private static final char[] white = new char[]{' ', '\t', '\n', '\r'};

    public JsonValue parseStream(InputStream is) throws IOException {

        JsonValue root = null;

        try (Reader reader = new InputStreamReader(is)) {

            while (true) {
                int next = reader.read();
                if (next == -1) {
                    break;
                }

                if (isWhite(next)) {
                    continue;
                }

                if (next == LEFT_CURLY) {
                    root = readObject(reader);
                }
            }
        }
        return root;
    }

    private JsonArray readArray(Reader reader) throws IOException {
        JsonArray array = new JsonArray();

        JsonValue value;
        while (true) {
            int next = reader.read();
            while (isWhite(next)) next = reader.read();
            if (next == RIGHT_SQUARE)
                return array;

            while (next == COMMA || isWhite(next)) next = reader.read();

            if ('\"' == next) {
                value = readString(reader);
            } else if (JsonParser.LEFT_CURLY == next) {
                value = readObject(reader);
            } else if (fromNumeric(next)) {
                value = readNumeric(next, reader);
            } else if (JsonParser.LEFT_SQUARE == next) {
                value = readArray(reader);
            } else if (next == 'n' || next == 't' || next == 'f') {
                value = readLiteral(next, reader);
            } else {
                value = null;
            }

            array.addValue(value);
        }
    }

    private JsonObject readObject(Reader reader) throws IOException {
        JsonObject obj = new JsonObject();

        String key;
        JsonValue value;
        while (true) {
            int next = reader.read();
            while (isWhite(next)) next = reader.read();
            if (next == RIGHT_CURLY)
                return obj;

            while (next == COMMA || isWhite(next)) next = reader.read();

            if ('"' != next) {
                throw new IllegalArgumentException("objects contains key value pairs");
            }
            key = readString(reader).value();
            next = reader.read();
            while (isWhite(next))
                next = reader.read();
            if (next != COLON) {
                throw new IllegalArgumentException("A colon should follow");
            }

            next = reader.read();
            while (isWhite(next)) next = reader.read();

            if ('\"' == next) {
                value = readString(reader);
            } else if (JsonParser.LEFT_CURLY == next) {
                value = readObject(reader);
            } else if (fromNumeric(next)) {
                value = readNumeric(next, reader);
            } else if (JsonParser.LEFT_SQUARE == next) {
                value = readArray(reader);
            } else if (next == 'n' || next == 't' || next == 'f') {
                value = readLiteral(next, reader);
            } else {
                value = null;
            }

            obj.addValue(key, value);
        }
    }

    private JsonValue readLiteral(int next, Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) next);
        while (true) {
            next = reader.read();
            if (!isWhite(next)) {
                sb.append((char) next);
                continue;
            }
            break;
        }
        String value = sb.toString();
        switch (value) {
            case mkTrue:
            case mkFalse:
            case mkNull:
                return new JsonLiteral(value);
        }
        return null;
    }

    private JsonValue readNumeric(int next, Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) next);

        while (true) {
            next = reader.read();
            if (fromNumeric(next)) {
                sb.append((char) next);
                continue;
            }
            break;
        }
        return new JsonNumeric(Double.parseDouble(sb.toString()));
    }

    private boolean fromNumeric(int next) {
        switch ((char) next) {
            case '+':
            case '-':
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

    private JsonString readString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int next = reader.read();
            if (next == -1) {
                throw new IllegalArgumentException("Unexpected end of string during parsing.");
            }
            if (next == '\"') {
                // end of string
                return new JsonString(sb.toString());
            }
            if (next == '\\') {
                sb.append((char) reader.read());
                continue;
            }
            sb.append((char) next);
        }
    }

    private boolean isWhite(int ch) {
        for (char w : white)
            if (ch == w)
                return true;
        return false;
    }

    public static void main(String[] args) throws IOException {
        String test = "{\n" +
                "    \"name\": \"Alice\",\n" +
                "    \"age\": 20,\n" +
                "    \"address\": {\n" +
                "        \"streetAddress\": {" +
                "               \"street\" : \"Wall Street\",\n" +
                "               \"number\" : 100\n" +
                "           }," +
                "        \"city\": \"New York\"\n" +
                "    }" +
                ",\n" +
                "    \"phoneNumber\": [\n" +
                "        {\n" +
                "            \"type\": \"home\",\n" +
                "            \"number\": \"212-333-1111\"\n" +
                "        },{\n" +
                "            \"type\": \"fax\",\n" +
                "            \"number\": \"646-444-2222\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        JsonParser p = new JsonParser();
        JsonValue root = p.parseStream(new ByteArrayInputStream(test.getBytes()));

        JsonPrettyPrint.printPretty(root);
    }
}
