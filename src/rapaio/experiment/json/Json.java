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

import rapaio.experiment.json.tree.JsonValue;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/20/15.
 */
public final class Json {

    public enum Format {
        FLAT,
        GZIP,
        LZ4JSON
    }

    public static Stream<JsonValue> stream(File root, Format format, FileFilter filter) {
        return Json.stream(root, format, filter, msg -> {
        });
    }

    public static Stream<JsonValue> stream(File root, Format format, FileFilter filter, MessageHandler ph) {
        List<File> files = new ArrayList<>();
        if (root.isDirectory()) {
            files = Arrays.asList(root.listFiles()).stream().filter(filter::accept).collect(Collectors.toList());
        } else {
            files.add(root);
        }
        switch (format) {
            case FLAT:
            case GZIP:
                JsonSpliterator spliterator1 = new ReaderJsonSpliterator(files, ph);
                return StreamSupport.stream(spliterator1, spliterator1.isParallel());
            case LZ4JSON:
                JsonSpliterator spliterator2 = new LzJsonSpliterator(files, ph);
                return StreamSupport.stream(spliterator2, spliterator2.isParallel());
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void write(OutputStream os, JsonValue js) throws IOException {
        Writer w = new OutputStreamWriter(os);
        w.append(js.toString()).append('\n');
        w.flush();
    }
}
