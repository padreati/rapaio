/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.io.json.stream;

import rapaio.io.json.tree.*;

import java.io.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/12/15.
 */
@Deprecated
public class JsonInputFlat implements JsonInput {

    private static final Logger logger = Logger.getLogger(JsonInputFlat.class.getName());

    private static final char LEFT_SQUARE = '[';
    private static final char RIGHT_SQUARE = ']';
    private static final char LEFT_CURLY = '{';
    private static final char RIGHT_CURLY = '}';
    private static final char COLON = ':';
    private static final char COMMA = ',';
    private static final String KEY_TRUE = "true";
    private static final String KEY_FALSE = "false";
    private static final String KEY_NULL = "null";

    private char[] buffer = new char[256];
    private int pos = 0;

    private final Reader reader;
    int _next = ' ';

    public JsonInputFlat(File file) throws IOException {
        this.reader = (file.getName().endsWith(".gz")) ?
                new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file), 1024 * 1024), 16 * 1024), "utf-8") :
                new InputStreamReader(new BufferedInputStream(new FileInputStream(file), 16 * 1024), "utf-8");
    }

    public JsonInputFlat(String text) throws IOException {
        this.reader = new CharArrayReader(text.toCharArray());
    }

    private boolean isNumeric(int ch) {
        if (ch >= '0' && ch <= '9')
            return true;
        switch (ch) {
            case '+':
            case '-':
            case '.':
            case 'e':
            case 'E':
                return true;
        }
        return false;
    }

    private boolean isWhite(int ch) {
        return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
    }

    private String getBuf() {
        return String.valueOf(buffer, 0, pos);
    }

    private int getNext() throws IOException {
        try {
            return reader.read();
        } catch (IOException ex) {
            return -1;
        }
    }

    private void skipWhite() throws IOException {
        while (isWhite(_next)) _next = getNext();
    }

    @Override
    public JsonValue read() throws IOException {
        while (true) {
            skipWhite();
            if (_next == -1) {
                break;
            }
            skipWhite();
            if (LEFT_CURLY == _next) {
                return readObject();
            } else if (LEFT_SQUARE == _next) {
                return readArray();
            } else if ('\"' == _next) {
                return readString();
            } else if (isNumeric(_next)) {
                return readNumeric();
            } else if (_next == 't' || _next == 'f') {
                return readBool();
            } else if (_next == 'n') {
                return readNull();
            }
        }
        return null;
    }

    private JsonArray readArray() throws IOException {
        JsonArray array = new JsonArray();

        _next = getNext();
        JsonValue value;
        while (true) {
            skipWhite();
            if (_next == RIGHT_SQUARE) {
                _next = ' ';
                return array;
            }
            while (_next == COMMA || isWhite(_next)) _next = getNext();

            if (LEFT_CURLY == _next) {
                value = readObject();
            } else if (LEFT_SQUARE == _next) {
                value = readArray();
            } else if ('\"' == _next) {
                value = readString();
            } else if (isNumeric(_next)) {
                value = readNumeric();
            } else if (_next == 't' || _next == 'f') {
                value = readBool();
            } else if (_next == 'n') {
                value = readNull();
            } else {
                value = null;
            }
            array.addValue(value);
        }
    }

    private JsonObject readObject() throws IOException {
        JsonObject obj = new JsonObject();

        _next = getNext();
        String key;
        while (true) {
            skipWhite();
            if (_next == RIGHT_CURLY) {
                _next = ' ';
                return obj;
            }
            while (_next == COMMA || isWhite(_next)) _next = getNext();

            if ('"' != _next) {
                throw new IllegalArgumentException("objects contains key value pairs, parsed object: " + obj.toString() + ", next char: " + ((char) _next));
            }
            key = readString().asString().get();
            while (isWhite(_next)) _next = getNext();

            if (_next != COLON) {
                throw new IllegalArgumentException("A colon should follow, object: " + obj + ", key: " + key);
            }

            _next = getNext();
            skipWhite();

            if ('\"' == _next) {
                obj.addValue(key, readString());
            } else if (LEFT_CURLY == _next) {
                obj.addValue(key, readObject());
            } else if (isNumeric(_next)) {
                obj.addValue(key, readNumeric());
            } else if (LEFT_SQUARE == _next) {
                obj.addValue(key, readArray());
            } else if (_next == 't' || _next == 'f') {
                obj.addValue(key, readBool());
            } else if (_next == 'n') {
                obj.addValue(key, readNull());
            } else {
                obj.addValue(key, null);
            }
        }
    }

    private JsonBool readBool() throws IOException {
        pos = 0;
        buffer[pos++] = (char) _next;
        while (true) {
            _next = getNext();
            if (isWhite(_next) || _next == COMMA ||
                    _next == COLON || _next == RIGHT_CURLY ||
                    _next == LEFT_CURLY || _next == RIGHT_SQUARE ||
                    _next == LEFT_SQUARE)
                break;
            if (pos == buffer.length) {
                char[] tmp = new char[buffer.length + buffer.length / 2];
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                buffer = tmp;
            }
            buffer[pos++] = (char) _next;
        }
        String value = getBuf();
        switch (value) {
            case KEY_TRUE:
            case KEY_FALSE:
                return new JsonBool(value);
        }
        logger.severe("parsing literal exception, parsed value: " + value);
        throw new IllegalArgumentException("parsing literal exception, parsed value: " + value);
    }

    private JsonNull readNull() throws IOException {
        pos = 0;
        buffer[pos++] = (char) _next;
        while (true) {
            _next = getNext();
            if (isWhite(_next) || _next == COMMA ||
                    _next == COLON || _next == RIGHT_CURLY ||
                    _next == LEFT_CURLY || _next == RIGHT_SQUARE ||
                    _next == LEFT_SQUARE)
                break;
            if (pos == buffer.length) {
                char[] tmp = new char[buffer.length + buffer.length / 2];
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                buffer = tmp;
            }
            buffer[pos++] = (char) _next;
        }
        String value = getBuf();
        switch (value) {
            case KEY_NULL:
                return JsonValue.NULL;
        }
        logger.severe("parsing literal exception, parsed value: " + value);
        throw new IllegalArgumentException("parsing literal exception, parsed value: " + value);
    }

    private JsonNumber readNumeric() throws IOException {
        pos = 0;
        buffer[pos++] = (char) _next;
        while (true) {
            _next = getNext();
            if (isNumeric(_next)) {
                if (pos == buffer.length) {
                    char[] tmp = new char[buffer.length + buffer.length / 2];
                    System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                    buffer = tmp;
                }
                buffer[pos++] = (char) _next;
                continue;
            }
            break;
        }
        return new JsonNumber(getBuf());
    }

    private JsonString readString() throws IOException {
        pos = 0;
        while (true) {
            _next = getNext();
            if (_next == -1 || _next == '\"') {
                _next = ' ';
                return new JsonString(getBuf());
            }
            if (_next == '\\') {
                if (pos == buffer.length) {
                    char[] tmp = new char[buffer.length + buffer.length / 2];
                    System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                    buffer = tmp;
                }
                buffer[pos++] = (char) _next;
                _next = getNext();
            }
            if (pos == buffer.length) {
                char[] tmp = new char[buffer.length + buffer.length / 2];
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                buffer = tmp;
            }
            buffer[pos++] = (char) _next;
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ingored) {
            }
        }
    }
}
