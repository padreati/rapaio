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
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Created by tutuianu on 3/6/15.
 */
public class ReaderJsonSpliterator implements JsonSpliterator {

    private static final Logger logger = Logger.getLogger(ReaderJsonSpliterator.class.getName());

    private static final char LEFT_SQUARE = '[';
    private static final char RIGHT_SQUARE = ']';
    private static final char LEFT_CURLY = '{';
    private static final char RIGHT_CURLY = '}';
    private static final char COLON = ':';
    private static final char COMMA = ',';
    private static final String KEY_TRUE = "true";
    private static final String KEY_FALSE = "false";
    private static final String KEY_NULL = "null";

    private LinkedList<File> files;
    private final MessageHandler ph;
    private final boolean parallel;
    private char[] buffer = new char[256];
    private int pos = 0;

    private long estimateSize = Long.MAX_VALUE;
    private Reader reader;
    int _next = ' ';

    public ReaderJsonSpliterator(List<File> files, MessageHandler ph) {
        this.files = new LinkedList<>(files);
        this.parallel = files.size() > 1;
        this.ph = ph;
        estimateSize = files.stream().mapToLong(File::length).sum();
    }

    @Override
    public boolean isParallel() {
        return parallel;
    }

    @Override
    public String[] getFileTypes() {
        return new String[]{".gz", ".json"};
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

    private Reader buildReader(File source) throws IOException {
        if (source.getName().endsWith(".gz")) {
            return new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(source), 1024 * 1024), 16 * 1024), "utf-8");
        }
        return new InputStreamReader(new BufferedInputStream(new FileInputStream(source), 16 * 1024), "utf-8");
    }

    private String getBuf() {
        return String.valueOf(buffer, 0, pos);
    }

    private int getNext() throws IOException {
        if (reader != null) {
            int next = reader.read();
            if (next == -1) {
                reader.close();
                if (files.isEmpty())
                    return -1;
                ph.sendMessage("parsing (next): " + files.getFirst().getName());
                estimateSize = files.stream().mapToLong(File::length).sum();
                reader = buildReader(files.pollFirst());
                return getNext();
            }
            return next;
        } else {
            if (files.isEmpty()) {
                return -1;
            }
            ph.sendMessage("parsing (head): " + files.getFirst().getName());
            estimateSize = files.stream().mapToLong(File::length).sum();
            reader = buildReader(files.pollFirst());
            return getNext();
        }
    }

    private void skipWhite() throws IOException {
        while (isWhite(_next)) _next = getNext();
    }

    private JsonValue parseStream() throws IOException {
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
            key = readString().stringValue();
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
    public boolean tryAdvance(Consumer<? super JsonValue> action) {
        try {
            JsonValue value = parseStream();
            if (value == null)
                return false;
            action.accept(value);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "error at try advance", ex);
            return false;
        }
        return true;
    }

    @Override
    public Spliterator<JsonValue> trySplit() {
        if (files.size() > 1) {
            int len = files.size() / 2;
            LinkedList<File> splitFiles = new LinkedList<>(files.subList(files.size() - len, files.size()));
            files = new LinkedList<>(files.subList(0, files.size() - len));
            return new ReaderJsonSpliterator(splitFiles, ph);
        }
        return null;
    }

    @Override
    public void forEachRemaining(Consumer<? super JsonValue> action) {
        while (true) {
            try {
                JsonValue value = parseStream();
                if (value == null)
                    return;
                action.accept(value);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "error at forEachRemaining", e);
                return;
            }
        }
    }

    @Override
    public long estimateSize() {
        return estimateSize;
    }

    @Override
    public int characteristics() {
        return CONCURRENT & SIZED & SUBSIZED & IMMUTABLE;
    }
}
