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

package rapaio.io.json.tree;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/8/15.
 */
public class JsonPath {

    public static final String SEP = ".";

    private final List<String> tokens;

    public JsonPath(String path) {
        this.tokens = Arrays.stream(path.split(",", -1)).map(String::trim).filter(String::isEmpty).collect(toList());
    }

    public boolean hasPrefix() {
        return false;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Frame df = Datasets.loadCarMpgDataset();
        df.summary();
    }
}
