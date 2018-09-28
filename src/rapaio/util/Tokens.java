/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Generic tuple glass.
 * Allows handling index based value, like a List but enriched with various interesting operations.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/10/15.
 */
public class Tokens implements Comparable<Tokens> {

    private final List<String> values = new ArrayList<>();

    public static Tokens from(String... tokens) {
        return new Tokens(tokens);
    }

    public static Tokens parse(String text) {
        return parse(text, true, true, true, true);
    }

    public static Tokens parse(String text, boolean removeSquares, boolean trim, boolean removeEmpty, boolean unquote) {
        if (trim)
            text = text.trim();
        if (text.startsWith("["))
            text = text.substring("[".length());
        if (text.endsWith("]"))
            text = text.substring(0, text.length() - "]".length());
        String[] tokens = text.split(",", -1);
        for (int i = 0; i < tokens.length; i++) {
            if (trim)
                tokens[i] = tokens[i].trim();
            if (unquote && tokens[i].startsWith("\"")) {
                tokens[i] = tokens[i].substring("\"".length());
            }
            if (unquote && tokens[i].endsWith("\"")) {
                tokens[i] = tokens[i].substring(0, tokens[i].length() - "\"".length());
            }
        }
        return new Tokens(Arrays.stream(tokens).filter(txt -> !removeEmpty || !txt.isEmpty()).collect(toList()));
    }

    private Tokens(List<String> args) {
        values.addAll(args);
    }

    private Tokens(String... args) {
        Collections.addAll(values, args);
    }

    public int size() {
        return values.size();
    }

    @Override
    public int compareTo(Tokens o) {
        int i = 0;
        while (true) {
            if (i == size() && i == o.size())
                return 0;
            if (i == size())
                return -1;
            if (i == o.size())
                return 1;
            int cmp = values.get(i).compareTo(o.values.get(i));
            if (cmp != 0)
                return cmp;
            i++;
        }
    }

    public boolean contains(Tokens o) {
        int pos = 0;
        for (String v : o.values) {
            if (pos == size()) {
                return false;
            }
            boolean in = false;
            while (true) {
                if (pos == values.size())
                    break;
                if (v.compareTo(values.get(pos)) == 0) {
                    in = true;
                    break;
                }
                pos++;
            }
            if (!in) {
                return false;
            }
        }
        return true;
    }

    public boolean overlaps(Tokens o) {
        return overlaps(o, 1);
    }

    public boolean overlaps(Tokens o, int offset) {
        if (size() != o.size()) {
            throw new IllegalArgumentException("overlapping tokens must have the same size");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset value must be greater or equal with 0");
        }
        if (offset == 0) {
            return compareTo(o) == 0;
        }
        if (offset >= size() || offset >= o.size()) {
            // then simply they can be joined
            return true;
        }
        for (int i = offset; i < size(); i++) {
            if (values.get(i).compareTo(o.values.get(i - offset)) != 0)
                return false;
        }
        return true;
    }

    public Tokens joinOverlap(Tokens o) {
        return joinOverlap(o, 1);
    }

    public Tokens joinOverlap(Tokens o, int offset) {
        if (offset >= size() || offset >= o.size()) {
            // then simply they can be joined
            List<String> v = new ArrayList<>(values);
            v.addAll(o.values);
            return new Tokens(v);
        }
        List<String> v = new ArrayList<>(values.subList(0, offset));
        v.addAll(o.values);
        return new Tokens(v);
    }

    public Stream<String> stream() {
        return values.stream();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && compareTo((Tokens) o) == 0;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    public String deepString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Tokens{" + Arrays.deepToString(values.toArray()) + "}";
    }
}
