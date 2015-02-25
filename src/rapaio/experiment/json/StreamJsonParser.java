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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/18/15.
 */
public final class StreamJsonParser implements JsonParser {

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

class JsonSpliterator implements Spliterator<JsonValue>, JsonParser {

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
            while (isWhite(_next)) _next = readers.getFirst().read();
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
            } else if (LEFT_CURLY == _next) {
                return readObject();
            } else if (isNumeric(_next)) {
                return readNumeric();
            } else if (LEFT_SQUARE == _next) {
                return readArray();
            } else if (_next == 'n' || _next == 't' || _next == 'f') {
                return readLiteral();
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
            if (_next == RIGHT_SQUARE) {
                _next = ' ';
                return array;
            }
            while (_next == COMMA || isWhite(_next)) _next = readers.getFirst().read();

            if ('\"' == _next) {
                value = readString();
            } else if (LEFT_CURLY == _next) {
                value = readObject();
            } else if (isNumeric(_next)) {
                value = readNumeric();
            } else if (LEFT_SQUARE == _next) {
                value = readArray();
            } else if (_next == 'n' || _next == 't' || _next == 'f') {
                value = readLiteral();
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
            if (_next == RIGHT_CURLY) {
                _next = ' ';
                return obj;
            }
            while (_next == COMMA || isWhite(_next)) _next = readers.getFirst().read();

            if ('"' != _next) {
                throw new IllegalArgumentException("objects contains key value pairs, parsed object: " + obj.toString() + ", next char: " + ((char) _next));
            }
            key = readString().stringValue();
            while (isWhite(_next)) _next = readers.getFirst().read();
            if (_next != COLON) {
                throw new IllegalArgumentException("A colon should follow, object: " + obj + ", key: " + key);
            }

            _next = readers.getFirst().read();
            skipWhite();

            if ('\"' == _next) {
                obj.addValue(key, readString());
            } else if (LEFT_CURLY == _next) {
                obj.addValue(key, readObject());
            } else if (isNumeric(_next)) {
                obj.addValue(key, readNumeric());
            } else if (LEFT_SQUARE == _next) {
                obj.addValue(key, readArray());
            } else if (_next == 'n' || _next == 't' || _next == 'f') {
                obj.addValue(key, readLiteral());
            } else {
                obj.addValue(key, null);
            }
        }
    }

    private JsonLeaf readLiteral() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) _next);
        while (true) {
            _next = readers.getFirst().read();
            if (isWhite(_next) || _next == COMMA || _next == COLON || _next == RIGHT_CURLY || _next == LEFT_CURLY || _next == RIGHT_SQUARE || _next == LEFT_SQUARE)
                break;
            sb.append((char) _next);
        }
        String value = sb.toString();
        switch (value) {
            case KEY_TRUE:
            case KEY_FALSE:
            case KEY_NULL:
                return new JsonLeaf(JsonType.LITERAL, value);
        }
        throw new IllegalArgumentException("parsing literal exception, parsed value: " + value);
    }

    private JsonLeaf readNumeric() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) _next);

        while (true) {
            _next = readers.getFirst().read();
            if (isNumeric(_next)) {
                sb.append((char) _next);
                continue;
            }
            break;
        }
        return new JsonLeaf(JsonType.NUMERIC, sb.toString());
    }

    private JsonLeaf readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            _next = readers.getFirst().read();
            if (_next == -1 || _next == '\"') {
                _next = ' ';
                return new JsonLeaf(JsonType.STRING, sb.toString());
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
