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

package rapaio.experiment.io.json.stream;

import rapaio.experiment.io.json.tree.*;
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
import java.util.zip.GZIPOutputStream;

import static java.util.stream.Collectors.*;

/**
 * Utility class able to write json values to an output stream formatted as lzjson.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/9/15.
 */
@Deprecated
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


    public LzJsonOutput(OutputStream os) throws IOException {
        this.os = new DataOutputStream(new GZIPOutputStream(os));
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
        encoding.writeInt(len, os);
    }

    private int countLen(int len) {
        return encoding.countLen(len);
    }

    private void writeBuff(byte[] buff) throws IOException {
        writeLen(buff.length);
        os.write(buff);
    }

    private int countBuff(byte[] buff) {
        return countLen(buff.length) + buff.length;
    }

    private void writeBuffer() throws IOException {

        // initialize levels
        strTerms = new String[0];
        strTermDict = new HashMap<>();
        strTermIndex = new HashMap<>();
        numTerms = new String[0];
        numTermDict = new HashMap<>();
        numTermIndex = new HashMap<>();

        // here we build the term index
        buildDictionary();

        // then we write the str term levels
        os.write(BLOCK_STR_TERM_LIST);
        os.writeInt(strTerms.length);
        for (String strTerm : strTerms) {
            writeBuff(strTermDict.get(strTerm));
        }

        // then we write the num term levels
        os.write(BLOCK_NUM_TERM_LIST);
        os.writeInt(numTerms.length);
        for (String numTerm : numTerms) {
            writeBuff(numTermDict.get(numTerm));
        }

        // then write all objects

        for (JsonValue js : objectBuffer) {
            writeAllObject(js, true);
        }

        objectBuffer.clear();
    }

	private void writeAllObject(JsonValue js, boolean write) throws IOException {
		os.writeByte(BLOCK_VALUE);
		if (js instanceof JsonNull) {
		    writeNull();
		    return;
		}
		if (js instanceof JsonBool) {
		    writeBool((JsonBool) js);
		    return;
		}
		if (js instanceof JsonNumber) {
		    writeNumeric((JsonNumber) js);
		    return;
		}
		if (js instanceof JsonString) {
		    writeString((JsonString) js);
		    return;
		}
		if (js instanceof JsonArray) {
		    writeArray((JsonArray) js);
		    return;
		}
		writeObject((JsonObject) js, write);
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

        List<Pair<String, Integer>> strlist = strCounter.entrySet().stream().map(e -> Pair.from(e.getKey(), e.getValue())).collect(toList());
        strlist.sort((o1, o2) -> -Integer.compare(o1._1.length() * o1._2, o2._1.length() * o2._2));
        for (int i = 0; i < strlist.size(); i++) {
            strTerms[i] = strlist.get(i)._1;
            strTermDict.put(strlist.get(i)._1, strlist.get(i)._1.getBytes());
            strTermIndex.put(strlist.get(i)._1, i);
        }

        List<Pair<String, Integer>> numlist = numCounter.entrySet().stream().map(e -> Pair.from(e.getKey(), e.getValue())).collect(toList());
        numlist.sort((o1, o2) -> -Integer.compare(o1._1.length() * o1._2, o2._1.length() * o2._2));
        for (int i = 0; i < numlist.size(); i++) {
            numTerms[i] = numlist.get(i)._1;
            numTermDict.put(numlist.get(i)._1, numlist.get(i)._1.getBytes());
            numTermIndex.put(numlist.get(i)._1, i);
        }
    }

    private void countJs(Map<String, Integer> counter, Map<String, Integer> numericCounter, JsonValue js) {
        if (js instanceof JsonString) {
            Integer c = counter.get(js.toString());
            if (c == null) {
                c = 0;
            }
            c++;
            counter.put(js.toString(), c);
            return;
        }
        if (js instanceof JsonArray) {
            js.valueStream().forEach(jss -> countJs(counter, numericCounter, jss));
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
                countJs(counter, numericCounter, obj.get(key));
            }
        }
        if (js instanceof JsonNumber) {
            String value = js.asString().get();
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
        os.writeByte(js.toString().equals("true") ? TYPE_TRUE : TYPE_FALSE);
    }

    private void writeNumeric(JsonNumber js) throws IOException {
        String value = js.asString().get();
        Integer pos = numTermIndex.get(value);
        if (pos != null) {
            os.writeByte(TYPE_NUMERIC_TERM);
            writeLen(pos);
            return;
        }
        os.writeByte(TYPE_NUMERIC);
        writeBuff(js.asString().get().getBytes());
    }

    private void writeString(JsonString js) throws IOException {
        String value = js.asString().get();
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
        List<JsonValue> valueList = array.valueList();
        int size = valueList.size();
        os.writeByte(TYPE_ARRAY);
        writeLen(size);
        for (JsonValue js : valueList) {
            writeAllObject(js, false);
        }
    }

    private void writeObject(JsonObject jsObject, boolean withLen) throws IOException {
        int size = jsObject.keySet().size();
        os.writeByte(TYPE_OBJECT);
        writeLen(size);
        for (String key : jsObject.keySet()) {
            writeString(new JsonString(key));
            if (withLen) {
                int len = computeLen(jsObject.get(key), 0);
                writeLen(len);
            }
            JsonValue js = jsObject.get(key);
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
            writeObject((JsonObject) js, false);
        }
    }

    private int computeLen(JsonValue js, int count) {
        if (js instanceof JsonNull) {
            return count + 1;
        }
        if (js instanceof JsonBool) {
            return count + 1;
        }
        if (js instanceof JsonNumber) {
            String value = js.asString().get();
            Integer pos = numTermIndex.get(value);
            if (pos != null) {
                return count + 1 + countLen(pos);
            }
            return count + 1 + countBuff(js.asString().get().getBytes());
        }
        if (js instanceof JsonString) {
            String value = js.asString().get();
            Integer pos = strTermIndex.get(value);
            if (pos != null) {
                return count + 1 + countLen(pos);
            }
            return count + 1 + countBuff(value.getBytes());
        }
        if (js instanceof JsonArray) {
            JsonArray array = (JsonArray) js;
            List<JsonValue> valueList = array.valueList();
            int size = valueList.size();
            count += 1 + countLen(size);
            for (JsonValue child : valueList) {
                count += computeLen(child, 0);
            }
            return count;
        }
        if (js instanceof JsonObject) {
            JsonObject jsObject = (JsonObject) js;
            int size = jsObject.keySet().size();
            count += 1;
            count += countLen(size);
            for (String key : jsObject.keySet()) {
                count += computeLen(new JsonString(key), 0);
                JsonValue child = jsObject.get(key);
                count += computeLen(child, 0);
            }
            return count;
        }
        return 0;
    }

    @Override
    public void close() throws IOException {
        if (!objectBuffer.isEmpty())
            writeBuffer();
        os.flush();
        os.close();
    }
}
