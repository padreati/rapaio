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

package rapaio.io.json;

import rapaio.io.json.stream.LzJsonOutput;
import rapaio.util.Pin;

import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for manipulating json files.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/12/15.
 */
public class JsonUtil {

    public static void convertToLz(File root, FileFilter fnf, Function<String, String> rename, Consumer<String> mh) {
        File[] children = root.listFiles(fnf);
        Arrays.stream(children).parallel().forEach(f -> {
            String newFileName = rename.apply(f.getAbsolutePath());
            File newFile = new File(newFileName);
            mh.accept("converting ... " + newFileName);
            try {
                LzJsonOutput out = new LzJsonOutput(new BufferedOutputStream(new FileOutputStream(newFile))).withMaxObjectBuffer(20_000);
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
        Pin<Integer> insideCounter = new Pin<>(0);
        Pin<LzJsonOutput> out = new Pin<>();
        Pin<Integer> fileCounter = new Pin<>(0);
        Json.stream(root, ff).sequential().forEach(js -> {
            try {
                if (out.isEmpty()) {
                    out.set(new LzJsonOutput(new BufferedOutputStream(new FileOutputStream(new File(root, prefix + "-" + fileCounter.get() + ".lzjson")))).withMaxObjectBuffer(5_000));
                }
                if (insideCounter.get() >= sliceCount) {
                    out.get().close();
                    messageHandler.accept("starting .. " + fileCounter.get());
                    fileCounter.set(fileCounter.get() + 1);
                    insideCounter.set(0);
                    out.set(new LzJsonOutput(new BufferedOutputStream(new FileOutputStream(new File(root, prefix + "-" + fileCounter.get() + ".lzjson")))).withMaxObjectBuffer(5_000));
                }
                out.get().write(js);
                insideCounter.set(insideCounter.get() + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            out.get().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
