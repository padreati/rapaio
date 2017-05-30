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

package rapaio.experiment.io.json;

import rapaio.experiment.io.json.stream.JsonInputFlat;
import rapaio.experiment.io.json.stream.LzJsonOutput;
import rapaio.experiment.io.json.tree.JsonValue;
import rapaio.experiment.util.stream.StreamUtil;
import rapaio.util.Pin;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for manipulating json files.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/12/15.
 */
public class JsonUtil {

    public static Optional<JsonValue> parseTextOpt(String text) {
        try {
            return Optional.of(new JsonInputFlat(text).read());
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    public static JsonValue parseText(String text) {
        try {
            return new JsonInputFlat(text).read();
        } catch (IOException ex) {
            return JsonValue.NULL;
        }
    }

    public static void convertToLz(File root, FileFilter fnf, Function<String, String> rename, Consumer<String> mh) {
        File[] children = root.listFiles(fnf);
        Arrays.stream(children).parallel().forEach(f -> {
            String newFileName = rename.apply(f.getAbsolutePath());
            File newFile = new File(newFileName);
            mh.accept("converting ... " + newFileName);
            try {
                LzJsonOutput out = new LzJsonOutput(new BufferedOutputStream(new FileOutputStream(newFile))).withMaxObjectBuffer(50_000);
                Json.stream(f, nf -> nf.getName().equals(f.getName()), Json.allFilter())
                        .forEach(js -> {
                            try {
                                out.write(js);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void balancedConvertToLz(File root, FileFilter ff, String prefix, int sliceCount, Consumer<String> messageHandler) {
        Pin<Integer> fileCounter = new Pin<>(0);
        Stream<JsonValue> stream = Json.stream(root, ff).parallel();
        StreamUtil.partition(stream, sliceCount).forEach(list -> {
                    Pin<Integer> counter = new Pin<>(0);
                    synchronized (fileCounter) {
                        counter.set(fileCounter.get());
                    }
                    try {
                        File file = new File(root, prefix + "-" + counter.get() + ".lzjson");
                        messageHandler.accept("write " + file.getName());
                        fileCounter.set(fileCounter.get() + 1);
                        LzJsonOutput out = new LzJsonOutput(new BufferedOutputStream(new FileOutputStream(file))).withMaxObjectBuffer(50_000);
                        for (JsonValue js : list) {
                            out.write(js);
                        }
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public static void balancedConvertToLz(Stream<JsonValue> stream, File root, String prefix, int sliceCount, Consumer<String> messageHandler) {
        Pin<Integer> fileCounter = new Pin<>(0);
        StreamUtil.partition(stream, sliceCount).forEach(list -> {
                    synchronized (fileCounter) {
                        try {
                            File file = new File(root, prefix + "-" + fileCounter.get() + ".lzjson");
                            messageHandler.accept("write " + file.getName());
                            fileCounter.set(fileCounter.get() + 1);
                            LzJsonOutput out = new LzJsonOutput(new BufferedOutputStream(new FileOutputStream(file))).withMaxObjectBuffer(50_000);
                            for (JsonValue js : list) {
                                out.write(js);
                            }
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}
