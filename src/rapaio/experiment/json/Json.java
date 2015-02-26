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

package rapaio.experiment.json;

import rapaio.experiment.json.tree.*;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/20/15.
 */
public final class Json {

    public static final char LEFT_SQUARE = '[';
    public static final char RIGHT_SQUARE = ']';
    public static final char LEFT_CURLY = '{';
    public static final char RIGHT_CURLY = '}';
    public static final char COLON = ':';
    public static final char COMMA = ',';
    public static final String KEY_TRUE = "true";
    public static final String KEY_FALSE = "false";
    public static final String KEY_NULL = "null";
    public static final char[] WHITE_CHARS = new char[]{' ', '\t', '\n', '\r'};

    static boolean isNumeric(int next) {
        switch ((char) next) {
            case '+':
            case '-':
            case '.':
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

    static boolean isWhite(int ch) {
        for (char w : WHITE_CHARS)
            if (ch == w)
                return true;
        return false;
    }

    public static Stream<JsonValue> stream(InputStream... iss) {
        return StreamSupport.stream(new JsonSpliterator(iss), true);
    }

    public static Stream<JsonValue> stream(File root) {
        List<File> files = new ArrayList<>();
        if (root.isDirectory()) {
            files = Arrays.asList(root.listFiles());
        } else {
            files.add(root);
        }

        try {
            InputStream[] inputs = new InputStream[files.size()];
            for (int i = 0; i < inputs.length; i++) {
                if (files.get(i).getName().endsWith(".gz")) {
                    inputs[i] = new GZIPInputStream(new FileInputStream(files.get(i)));
                } else {
                    inputs[i] = new FileInputStream(files.get(i));
                }
            }
            return stream(inputs);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("matched files must exist and be accesible");
        }
    }

    public static void write(OutputStream os, JsonValue js) throws IOException {
        Writer w = new OutputStreamWriter(os);
        w.append(js.toString()).append('\n');
        w.flush();
//        w.close();
    }

}

class JsonSpliterator implements Spliterator<JsonValue> {

    private LinkedList<Reader> readers;
    int _next = ' ';

    public JsonSpliterator(InputStream[] iss) {
        this.readers = new LinkedList<>();
        for (InputStream is : iss) {
            readers.add(new InputStreamReader(is));
        }
    }

    private JsonSpliterator(LinkedList<Reader> readers) {
        _next = ' ';
        this.readers = readers;
    }

    private void skipWhite() throws IOException {
        if (!readers.isEmpty())
            while (Json.isWhite(_next)) _next = readers.getFirst().read();
    }

    private JsonValue parseStream() throws IOException {

        if (readers.isEmpty()) {
            return null;
        }
        while (true) {
            skipWhite();
            if (_next == -1) {
                if (!readers.isEmpty())
                    readers.pollFirst();
                break;
            }
            skipWhite();
            if ('\"' == _next) {
                return readString();
            } else if (Json.LEFT_CURLY == _next) {
                return readObject();
            } else if (Json.isNumeric(_next)) {
                return readNumeric();
            } else if (Json.LEFT_SQUARE == _next) {
                return readArray();
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

        _next = readers.getFirst().read();
        JsonValue value;
        while (true) {
            skipWhite();
            if (_next == Json.RIGHT_SQUARE) {
                _next = ' ';
                return array;
            }
            while (_next == Json.COMMA || Json.isWhite(_next)) _next = readers.getFirst().read();

            if ('\"' == _next) {
                value = readString();
            } else if (Json.LEFT_CURLY == _next) {
                value = readObject();
            } else if (Json.isNumeric(_next)) {
                value = readNumeric();
            } else if (Json.LEFT_SQUARE == _next) {
                value = readArray();
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

        _next = readers.getFirst().read();
        String key;
        while (true) {
            skipWhite();
            if (_next == Json.RIGHT_CURLY) {
                _next = ' ';
                return obj;
            }
            while (_next == Json.COMMA || Json.isWhite(_next)) _next = readers.getFirst().read();

            if ('"' != _next) {
                throw new IllegalArgumentException("objects contains key value pairs, parsed object: " + obj.toString() + ", next char: " + ((char) _next));
            }
            key = readString().stringValue();
            while (Json.isWhite(_next)) _next = readers.getFirst().read();
            if (_next != Json.COLON) {
                throw new IllegalArgumentException("A colon should follow, object: " + obj + ", key: " + key);
            }

            _next = readers.getFirst().read();
            skipWhite();

            if ('\"' == _next) {
                obj.addValue(key, readString());
            } else if (Json.LEFT_CURLY == _next) {
                obj.addValue(key, readObject());
            } else if (Json.isNumeric(_next)) {
                obj.addValue(key, readNumeric());
            } else if (Json.LEFT_SQUARE == _next) {
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
        StringBuilder sb = new StringBuilder();
        sb.append((char) _next);
        while (true) {
            _next = readers.getFirst().read();
            if (Json.isWhite(_next) || _next == Json.COMMA ||
                    _next == Json.COLON || _next == Json.RIGHT_CURLY ||
                    _next == Json.LEFT_CURLY || _next == Json.RIGHT_SQUARE ||
                    _next == Json.LEFT_SQUARE)
                break;
            sb.append((char) _next);
        }
        String value = sb.toString();
        switch (value) {
            case Json.KEY_TRUE:
            case Json.KEY_FALSE:
                return new JsonBool(value);
        }
        throw new IllegalArgumentException("parsing literal exception, parsed value: " + value);
    }

    private JsonNull readNull() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) _next);
        while (true) {
            _next = readers.getFirst().read();
            if (Json.isWhite(_next) || _next == Json.COMMA ||
                    _next == Json.COLON || _next == Json.RIGHT_CURLY ||
                    _next == Json.LEFT_CURLY || _next == Json.RIGHT_SQUARE ||
                    _next == Json.LEFT_SQUARE)
                break;
            sb.append((char) _next);
        }
        String value = sb.toString();
        switch (value) {
            case Json.KEY_NULL:
                return JsonValue.NULL;
        }
        throw new IllegalArgumentException("parsing literal exception, parsed value: " + value);
    }

    private JsonNumber readNumeric() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) _next);

        while (true) {
            _next = readers.getFirst().read();
            if (Json.isNumeric(_next)) {
                sb.append((char) _next);
                continue;
            }
            break;
        }
        return new JsonNumber(sb.toString());
    }

    private JsonString readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            _next = readers.getFirst().read();
            if (_next == -1 || _next == '\"') {
                _next = ' ';
                return new JsonString(sb.toString());
            }
            if (_next == '\\') {
                sb.append((char) _next);
                _next = readers.getFirst().read();
            }
            sb.append((char) _next);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super JsonValue> action) {
        try {
            JsonValue value = parseStream();
            if (value == null)
                return false;
            action.accept(value);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    public Spliterator<JsonValue> trySplit() {
        if (readers.size() > 1) {
            int len = readers.size() / 2;
            LinkedList<Reader> splitReaders = new LinkedList<>(readers.subList(readers.size() - len, readers.size()));
            readers = new LinkedList<>(readers.subList(0, readers.size() - len));
            return new JsonSpliterator(splitReaders);
        }
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return CONCURRENT;
    }
}
