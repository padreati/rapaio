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

import rapaio.experiment.io.json.tree.JsonValue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * JsonSpliterator
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/6/15.
 */
public class JsonSpliterator implements Spliterator<JsonValue> {

    private static final Logger logger = Logger.getLogger(JsonSpliterator.class.getName());

    private LinkedList<File> files;
    private final Consumer<String> messageHandler;
    private final boolean parallel;
    private final Predicate<String> propFilter;
    private long estimateSize = Long.MAX_VALUE;

    private JsonInput input;

    public JsonSpliterator(List<File> files, Consumer<String> messageHandler, Predicate<String> propFilter) {
        this.files = new LinkedList<>(files);
        this.parallel = files.size() > 1;
        this.messageHandler = messageHandler;
        this.propFilter = propFilter;
        estimateSize = files.stream().mapToLong(File::length).sum();
    }

    public boolean isParallel() {
        return parallel;
    }

    private JsonValue parseStream() throws IOException {
        if (input != null) {
            JsonValue js;
            try {
                js = input.read();
            } catch (IOException eof) {
                js = null;
            }
            if (js != null)
                return js;
            input.close();
            if (files.isEmpty())
                return null;
            messageHandler.accept("parsing (next): " + files.getFirst().getName());
            estimateSize = files.stream().mapToLong(File::length).sum();
            input = buildInput(files.pollFirst());
            return parseStream();
        } else {
            if (files.isEmpty()) {
                return null;
            }
            messageHandler.accept("parsing (head): " + files.getFirst().getName());
            estimateSize = files.stream().mapToLong(File::length).sum();
            input = buildInput(files.pollFirst());
            return parseStream();
        }
    }

    private JsonInput buildInput(File file) throws IOException {
        if (file.getName().endsWith(".lzjson"))
            return new LzJsonInput(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))), propFilter);
        return new JsonInputFlat(file);
    }

    @Override
    public boolean tryAdvance(Consumer<? super JsonValue> action) {
        try {
            JsonValue value = parseStream();
            if (value == null)
                return false;
            action.accept(value);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "error at try advance", ex);
            return false;
        }
        return true;
    }

    @Override
    public Spliterator<JsonValue> trySplit() {
        if (files.size() > 1) {
            int len = files.size() / 2;
            LinkedList<File> splitFiles = new LinkedList<>(files.subList(files.size() - len, files.size()));
            files = new LinkedList<>(files.subList(0, files.size() - len));
            return new JsonSpliterator(splitFiles, messageHandler, propFilter);
        }
        return null;
    }

    @Override
    public void forEachRemaining(Consumer<? super JsonValue> action) {
        while (true) {
            try {
                JsonValue value = parseStream();
                if (value == null)
                    return;
                action.accept(value);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "error at forEachRemaining", e);
                return;
            }
        }
    }

    @Override
    public long estimateSize() {
        return estimateSize;
    }

    @Override
    public int characteristics() {
        return SIZED & SUBSIZED & IMMUTABLE;
    }
}
