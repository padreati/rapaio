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

package rapaio.experiment.io.json.stream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import rapaio.experiment.io.json.tree.JsonArray;
import rapaio.experiment.io.json.tree.JsonNumber;
import rapaio.experiment.io.json.tree.JsonObject;
import rapaio.experiment.io.json.tree.JsonString;
import rapaio.experiment.io.json.tree.JsonValue;

/**
 * Utility class able to produce json values from an input stream formatted as lzjson.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/9/15.
 */
@Deprecated
public class LzJsonInput extends LzJsonAlgorithm implements JsonInput {

    private final DataInputStream is;
    private final List<String> strTermList = new ArrayList<>();
    private final List<String> numTermList = new ArrayList<>();
    private final Predicate<String> propFilter;

    public LzJsonInput(InputStream is, Predicate<String> propFilter) {
        this.is = new DataInputStream(is);
        this.propFilter = propFilter;
    }

    private boolean shouldParse(String key) {
        return propFilter.test(key);
    }

    private int readInt() throws IOException {
        return encoding.readInt(is);
    }

    private byte[] readBuff() throws IOException {
        int len = readInt();
        byte[] buff = new byte[len];
        is.readFully(buff);
        return buff;
    }

    public JsonValue read() throws IOException {
        byte cmd = is.readByte();
        if (cmd == BLOCK_STR_TERM_LIST) {
            // start reading levels
            strTermList.clear();
            int size = is.readInt();
            for (int i = 0; i < size; i++) {
                strTermList.add(new String(readBuff()));
            }
            cmd = is.readByte();
        }
        if (cmd == BLOCK_NUM_TERM_LIST) {
            // start reading levels
            numTermList.clear();
            int size = is.readInt();
            for (int i = 0; i < size; i++) {
                numTermList.add(new String(readBuff()));
            }
            //cmd =
            is.readByte();
        }
        byte type = is.readByte();
        return switch (type) {
            case TYPE_NULL -> VALUE_NULL;
            case TYPE_TRUE -> VALUE_TRUE;
            case TYPE_FALSE -> VALUE_FALSE;
            case TYPE_NUMERIC -> readNumber();
            case TYPE_NUMERIC_TERM -> readNumTerm();
            case TYPE_STRING -> readString();
            case TYPE_STRING_TERM -> readStringTerm();
            case TYPE_ARRAY -> readArray();
            case TYPE_OBJECT -> readObject(true);
            default -> null;
        };
    }

    private JsonNumber readNumber() throws IOException {
        int len = is.readByte();
        byte[] buff = new byte[len];
        is.readFully(buff);
        return new JsonNumber(new String(buff));
    }

    private JsonString readString() throws IOException {
        return new JsonString(new String(readBuff()));
    }

    private JsonNumber readNumTerm() throws IOException {
        return new JsonNumber(numTermList.get(readInt()));
    }

    private JsonString readStringTerm() throws IOException {
        return new JsonString(strTermList.get(readInt()));
    }

    private JsonArray readArray() throws IOException {
        int size = readInt();
        JsonArray array = new JsonArray();
        for (int i = 0; i < size; i++) {
            byte type = is.readByte();
            switch (type) {
                case TYPE_NULL -> array.addValue(VALUE_NULL);
                case TYPE_TRUE -> array.addValue(VALUE_TRUE);
                case TYPE_FALSE -> array.addValue(VALUE_FALSE);
                case TYPE_STRING -> array.addValue(readString());
                case TYPE_STRING_TERM -> array.addValue(readStringTerm());
                case TYPE_ARRAY -> array.addValue(readArray());
                case TYPE_OBJECT -> array.addValue(readObject(false));
                case TYPE_NUMERIC -> array.addValue(readNumber());
                case TYPE_NUMERIC_TERM -> array.addValue(readNumTerm());
            }
        }
        return array;
    }

    private JsonObject readObject(boolean withLen) throws IOException {
        JsonObject object = new JsonObject();
        int size = readInt();
        for (int i = 0; i < size; i++) {
            byte type = is.readByte();
            String key = switch (type) {
                case TYPE_STRING -> readString().asString().get();
                case TYPE_STRING_TERM -> readStringTerm().asString().get();
                default -> throw new IOException("invalid type for key of the object, type: " + type + ", object: " + object);
            };
            if (withLen) {
                int len = readInt();
                if (!shouldParse(key)) {
                    is.skipBytes(len);
                    continue;
                }
            }

            type = is.readByte();
            switch (type) {
                case TYPE_STRING -> object.addValue(key, readString());
                case TYPE_STRING_TERM -> object.addValue(key, readStringTerm());
                case TYPE_ARRAY -> object.addValue(key, readArray());
                case TYPE_OBJECT -> object.addValue(key, readObject(false));
                case TYPE_NULL -> object.addValue(key, VALUE_NULL);
                case TYPE_TRUE -> object.addValue(key, VALUE_TRUE);
                case TYPE_FALSE -> object.addValue(key, VALUE_FALSE);
                case TYPE_NUMERIC -> object.addValue(key, readNumber());
                case TYPE_NUMERIC_TERM -> object.addValue(key, readNumTerm());
            }
        }
        return object;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
