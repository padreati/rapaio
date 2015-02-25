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

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/20/15.
 */
public class JsonLeaf extends JsonValue {

    public static JsonLeaf NULL = new JsonLeaf(JsonType.LITERAL, "null");

    private final JsonType type;
    private final String source;

    private final double numericValue;
    private final String literalValue;
    private final String stringValue;

    public JsonLeaf(JsonType type, String source) {
        this.type = type;
        this.source = source;
        switch (type) {
            case ARRAY:
            case OBJECT:
                throw new IllegalArgumentException("Not allowed objects or arrays as leaves");
            case NUMERIC:
                numericValue = Double.parseDouble(source);
                literalValue = null;
                stringValue = null;
                break;
            case LITERAL:
                numericValue = Double.NaN;
                literalValue = source;
                stringValue = null;
                break;
            default:
                numericValue = Double.NaN;
                literalValue = null;
                stringValue = source;
        }
    }

    @Override
    public JsonType type() {
        return type;
    }

    @Override
    public String stringValue() {
        return source;
    }

    public double numericValue() {
        return numericValue;
    }

    @Override
    public String stringValue(String key) {
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonLeaf jsonLeaf = (JsonLeaf) o;

        if (Double.compare(jsonLeaf.numericValue, numericValue) != 0) return false;
        if (type != jsonLeaf.type) return false;
        if (literalValue != null ? !literalValue.equals(jsonLeaf.literalValue) : jsonLeaf.literalValue != null)
            return false;
        return !(stringValue != null ? !stringValue.equals(jsonLeaf.stringValue) : jsonLeaf.stringValue != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = type != null ? type.hashCode() : 0;
        temp = Double.doubleToLongBits(numericValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (literalValue != null ? literalValue.hashCode() : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        switch (type) {
            case NUMERIC:
            case LITERAL:
                return source;
            default:
                return '\"' + source + '\"';
        }
    }
}
