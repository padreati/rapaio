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

import rapaio.WS;
import rapaio.experiment.json.tree.JsonArray;
import rapaio.experiment.json.tree.JsonLeaf;
import rapaio.experiment.json.tree.JsonObject;
import rapaio.experiment.json.tree.JsonValue;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/17/15.
 */
public class JsonPrettyPrint {

    public static void printPretty(JsonValue json) {
        printPretty(0, json);
    }

    private static void printPretty(int level, JsonValue json) {
        if (json instanceof JsonLeaf) {
            JsonLeaf leaf = (JsonLeaf) json;
            switch (leaf.type()) {
                case LITERAL:
                    WS.print(leaf.stringValue());
                    return;
                case NUMERIC:
                    WS.print("" + leaf.stringValue());
                    return;
                default:
                    WS.print("\"" + leaf.stringValue() + "\"");
                    return;
            }
        }
        if (json instanceof JsonObject) {
            JsonObject obj = json.object();
            WS.print(tabs(level) + "{\n");
            boolean first = true;
            for (String key : obj.keySet()) {
                JsonValue value = obj.getValue(key);
                if (value instanceof JsonObject || value instanceof JsonArray) {
                    WS.print(tabs(level + 1) + "\"" + key + "\" :\n");
                    printPretty(level + 1, obj.getValue(key));
                } else {
                    WS.print(tabs(level + 1) + "\"" + key + "\" : ");
                    printPretty(level + 1, obj.getValue(key));
                }
                if (first) {
                    first = false;
                    WS.print(",");
                }
                WS.print("\n");
            }
            WS.print(tabs(level) + "}");
            return;
        }
        if (json instanceof JsonArray) {
            JsonArray arr = json.array();
            WS.print(tabs(level) + "[\n");
            List<JsonValue> keys = arr.values();
            for (int i = 0; i < keys.size(); i++) {
                JsonValue value = keys.get(i);
                if (!(value instanceof JsonObject) && !(value instanceof JsonArray))
                    WS.print(tabs(level + 1));
                printPretty(level + 1, value);
                if (i != keys.size() - 1) {
                    WS.print(",");
                }
                WS.print("\n");
            }
            WS.print(tabs(level) + "]");
        }
    }

    private static String tabs(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }
}
