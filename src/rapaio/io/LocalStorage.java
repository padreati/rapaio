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
 *
 */

package rapaio.io;

import rapaio.data.Frame;
import rapaio.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class LocalStorage {

    private final File root;
    private final Map<String, Csv> csvMap = new HashMap<>();

    public LocalStorage(String root) {
        this.root = new File(root);
    }

    public void addCsv(Csv csv) {
        addCsv("", csv);
    }

    public void addCsv(String key, Csv csv) {
        csvMap.put(key, csv);
    }

    public void csvStore(Frame df, String fileName) throws IOException {
        csvStore("", df, fileName);
    }

    public void csvStore(String key, Frame df, String fileName) throws IOException {
        if (!csvMap.containsKey(key)) {
            throw new IllegalArgumentException("There is not csv configuration with this name");
        }
        Csv csv = csvMap.get(key);
        csv.write(df, new File(root, fileName));
    }

    public Frame csvLoad(String fileName) throws IOException {
        return csvLoad("", fileName);
    }

    public Frame csvLoad(String key, String fileName) throws IOException {
        return csvLoad(key, fileName, null);
    }

    public Frame csvLoad(String key, String fileName, Frame template) throws IOException {
        if (!csvMap.containsKey(key)) {
            throw new IllegalArgumentException("There is not csv configuration with this name");
        }
        Csv csv = csvMap.get(key);
        csv.withTemplate(template);
        return csv.read(fileName);
    }

    public void javaStore(String key, Frame df, String fileName) throws IOException {
        JavaIO.storeToFile(df, new File(root, fileName));
    }

    public Frame javaLoadFrame(String key, String fileName) throws IOException, ClassNotFoundException {
        return (Frame) JavaIO.restoreFromFile(new File(root, fileName));
    }

    public Object javaLoad(String key, String fileName) throws IOException, ClassNotFoundException {
        return JavaIO.restoreFromFile(new File(root, fileName));
    }
}
