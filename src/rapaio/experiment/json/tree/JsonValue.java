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

package rapaio.experiment.json.tree;

import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/16/15.
 */
public abstract class JsonValue {

    public abstract JsonType type();

    public JsonObject object() {
        return (JsonObject) this;
    }

    public JsonArray array() {
        return (JsonArray) this;
    }

    public JsonLeaf leaf() {
        return (JsonLeaf) this;
    }

    public JsonValue getValue(String key) {
        return JsonObject.NULL;
    }

    public Set<String> keySet() {
        return object().keySet();
    }

    public String singleKey() {
        if (object().keySet().size() == 1)
            return object().keySet().iterator().next();
        return "";
    }

    public abstract String stringValue(String key);

    public abstract String stringValue();

    public boolean isObject() {
        return this instanceof JsonObject;
    }
}
