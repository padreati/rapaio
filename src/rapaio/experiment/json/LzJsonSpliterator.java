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
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/6/15.
 */
public class LzJsonSpliterator implements JsonSpliterator {

    private final MessageHandler ph;
    private LinkedList<File> files;
    private LzJsonInput input;
    long estimatedSize;

    public LzJsonSpliterator(List<File> files, MessageHandler ph) {
        this.files = new LinkedList<>(files);
        this.ph = ph;
        this.estimatedSize = files.stream().mapToLong(File::length).sum();
    }

    @Override
    public boolean isParallel() {
        return files.size() > 1;
    }

    private JsonValue readNext() throws IOException {
        if (input == null) {
            if (files.isEmpty())
                return null;
            File first = files.pollFirst();
            ph.sendMessage("start processing " + first.getName());
            input = new LzJsonInput(new BufferedInputStream(new FileInputStream(first)));
            estimatedSize = files.stream().mapToLong(File::length).sum();
        }
        try {
            JsonValue js = input.read();
            if (js != null) {
                return js;
            }
        } catch (EOFException eof) {
            input = null;
        }
        return readNext();
    }

    @Override
    public boolean tryAdvance(Consumer<? super JsonValue> action) {
        try {
            JsonValue js = readNext();
            if (js != null) {
                action.accept(js);
                return true;
            }
        } catch (IOException ioex) {
            ph.sendMessage(ioex.getMessage());
        }
        return false;
    }

    @Override
    public Spliterator<JsonValue> trySplit() {
        if (files.size() <= 1)
            return null;
        int mid = files.size() / 2;
        ph.sendMessage("split(0," + files.size() + ") -> (" + 0 + "," + mid + ") (" + mid + "," + files.size() + ")");
        Spliterator<JsonValue> split = new LzJsonSpliterator(files.subList(mid, files.size()), ph);
        files = new LinkedList<>(files.subList(0, mid));
        return split;
    }

    @Override
    public void forEachRemaining(Consumer<? super JsonValue> action) {
        while (true) {
            try {
                JsonValue js = readNext();
                if (js == null)
                    break;
                action.accept(js);
            } catch (IOException ex) {
                ph.sendMessage(ex.getMessage());
            }
        }
    }

    @Override
    public long estimateSize() {
        return estimatedSize;
    }

    @Override
    public int characteristics() {
        return CONCURRENT & SIZED & SUBSIZED & IMMUTABLE;
    }
}
