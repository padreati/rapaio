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
import rapaio.util.Pair;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Utility class able to write json values to an output stream formatted as lzjson.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/9/15.
 */
public class LzJsonOutput extends LzJsonAlgorithm implements Closeable {

    private final DataOutputStream os;
    private LinkedList<JsonValue> objectBuffer = new LinkedList<>();
    private int maxObjectBuffer = 1_000;
    //
    private String[] strTerms;
    private Map<String, byte[]> strTermDict = new HashMap<>();
    private Map<String, Integer> strTermIndex = new HashMap<>();
    private String[] numTerms;
    private Map<String, byte[]> numTermDict = new HashMap<>();
    private Map<String, Integer> numTermIndex = new HashMap<>();


    public LzJsonOutput(OutputStream os) {
        this.os = new DataOutputStream(os);
    }

    public LzJsonOutput withMaxObjectBuffer(int maxObjectBuffer) {
        this.maxObjectBuffer = maxObjectBuffer;
        return this;
    }

    public void write(JsonValue js) throws IOException {
        objectBuffer.add(js);
        if (objectBuffer.size() >= maxObjectBuffer) {
            writeBuffer();
        }
    }

    private void writeLen(int len) throws IOException {
        while (len >= 255) {
            os.writeByte(255);
            len -= 255;
        }
        os.writeByte(len);
    }

    private void writeBuff(byte[] buff) throws IOException {
        writeLen(buff.length);
        os.write(buff);
    }

    private void writeBuffer() throws IOException {

        // initialize dictionary
        strTerms = new String[0];
        strTermDict = new HashMap<>();
        strTermIndex = new HashMap<>();
        numTerms = new String[0];
        numTermDict = new HashMap<>();
        numTermIndex = new HashMap<>();

        // here we build the term index
        buildDictionary();

        // then we write the str term dictionary
        os.write(BLOCK_STR_TERM_LIST);
        os.writeInt(strTerms.length);
        for (String strTerm : strTerms) {
            writeBuff(strTermDict.get(strTerm));
        }

        // then we write the num term dictionary
        os.write(BLOCK_NUM_TERM_LIST);
        os.writeInt(numTerms.length);
        for (String numTerm : numTerms) {
            writeBuff(numTermDict.get(numTerm));
        }

        // then write all objects

        for (JsonValue js : objectBuffer) {
            os.writeByte(BLOCK_VALUE);
            if (js instanceof JsonNull) {
                writeNull();
                continue;
            }
            if (js instanceof JsonBool) {
                writeBool((JsonBool) js);
                continue;
            }
            if (js instanceof JsonNumber) {
                writeNumeric((JsonNumber) js);
                continue;
            }
            if (js instanceof JsonString) {
                writeString((JsonString) js);
                continue;
            }
            if (js instanceof JsonArray) {
                writeArray((JsonArray) js);
                continue;
            }
            writeObject((JsonObject) js);
        }

        objectBuffer.clear();
    }

    private void buildDictionary() {
        Map<String, Integer> strCounter = new HashMap<>();
        Map<String, Integer> numCounter = new HashMap<>();
        for (JsonValue js : objectBuffer) {
            countJs(strCounter, numCounter, js);
        }
        strCounter = strCounter.entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .filter(e -> e.getKey().length() > 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        numCounter = numCounter.entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .filter(e -> e.getKey().length() > 2)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        strTerms = new String[strCounter.size()];
        strTermDict = new HashMap<>();
        strTermIndex = new HashMap<>();

        numTerms = new String[numCounter.size()];
        numTermDict = new HashMap<>();
        numTermIndex = new HashMap<>();

        List<Pair<String, Integer>> strlist = strCounter.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).collect(toList());
        strlist.sort((o1, o2) -> -Integer.compare(o1.first.length() * o1.second, o2.first.length() * o2.second));
        for (int i = 0; i < strlist.size(); i++) {
            strTerms[i] = strlist.get(i).first;
            strTermDict.put(strlist.get(i).first, strlist.get(i).first.getBytes());
            strTermIndex.put(strlist.get(i).first, i);
        }

        List<Pair<String, Integer>> numlist = numCounter.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).collect(toList());
        numlist.sort((o1, o2) -> -Integer.compare(o1.first.length() * o1.second, o2.first.length() * o2.second));
        for (int i = 0; i < numlist.size(); i++) {
            numTerms[i] = numlist.get(i).first;
            numTermDict.put(numlist.get(i).first, numlist.get(i).first.getBytes());
            numTermIndex.put(numlist.get(i).first, i);
        }
    }

    private void countJs(Map<String, Integer> counter, Map<String, Integer> numericCounter, JsonValue js) {
        if (js instanceof JsonString) {
            Integer c = counter.get(js.stringValue());
            if (c == null) {
                c = 0;
            }
            c++;
            counter.put(js.stringValue(), c);
            return;
        }
        if (js instanceof JsonArray) {
            ((JsonArray) js).values().stream().forEach(jss -> countJs(counter, numericCounter, jss));
            return;
        }
        if (js instanceof JsonObject) {
            JsonObject obj = (JsonObject) js;
            for (String key : obj.keySet()) {
                Integer c = counter.get(key);
                if (c == null) {
                    c = 0;
                }
                c++;
                counter.put(key, c);
                countJs(counter, numericCounter, obj.getValue(key));
            }
        }
        if (js instanceof JsonNumber) {
            String value = js.stringValue();
            Integer c = numericCounter.get(value);
            if (c == null) {
                c = 0;
            }
            c++;
            numericCounter.put(value, c);
        }
    }

    private void writeNull() throws IOException {
        os.writeByte(TYPE_NULL);
    }

    private void writeBool(JsonBool js) throws IOException {
        os.writeByte(js.stringValue().equals("true") ? TYPE_TRUE : TYPE_FALSE);
    }

    private void writeNumeric(JsonNumber js) throws IOException {
        String value = js.stringValue();
        Integer pos = numTermIndex.get(value);
        if (pos != null) {
            os.writeByte(TYPE_NUMERIC_TERM);
            writeLen(pos);
            return;
        }
        os.writeByte(TYPE_NUMERIC);
        writeBuff(js.stringValue().getBytes());
    }

    private void writeString(JsonString js) throws IOException {
        String value = js.stringValue();
        Integer pos = strTermIndex.get(value);
        if (pos != null) {
            os.writeByte(TYPE_STRING_TERM);
            writeLen(pos);
            return;
        }
        os.writeByte(TYPE_STRING);
        writeBuff(value.getBytes());
    }

    private void writeArray(JsonArray array) throws IOException {
        int size = array.values().size();
        os.writeByte(TYPE_ARRAY);
        writeLen(size);
        for (JsonValue js : array.values()) {
            if (js instanceof JsonNull) {
                writeNull();
                continue;
            }
            if (js instanceof JsonBool) {
                writeBool((JsonBool) js);
                continue;
            }
            if (js instanceof JsonNumber) {
                writeNumeric((JsonNumber) js);
                continue;
            }
            if (js instanceof JsonString) {
                writeString((JsonString) js);
                continue;
            }
            if (js instanceof JsonArray) {
                writeArray((JsonArray) js);
                continue;
            }
            writeObject((JsonObject) js);
        }
    }

    private void writeObject(JsonObject jsObject) throws IOException {
        int size = jsObject.keySet().size();
        os.writeByte(TYPE_OBJECT);
        writeLen(size);
        for (String key : jsObject.keySet()) {
            writeString(new JsonString(key));
            JsonValue js = jsObject.getValue(key);
            if (js instanceof JsonNull) {
                writeNull();
                continue;
            }
            if (js instanceof JsonBool) {
                writeBool((JsonBool) js);
                continue;
            }
            if (js instanceof JsonNumber) {
                writeNumeric((JsonNumber) js);
                continue;
            }
            if (js instanceof JsonString) {
                writeString((JsonString) js);
                continue;
            }
            if (js instanceof JsonArray) {
                writeArray((JsonArray) js);
                continue;
            }
            writeObject((JsonObject) js);
        }
    }

    @Override
    public void close() throws IOException {
        if (!objectBuffer.isEmpty())
            writeBuffer();
        if (os != null) {
            os.flush();
            os.close();
        }
    }
}
