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

package rapaio.ml.features;

import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.Filters;
import rapaio.io.Csv;
import rapaio.sys.WS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static rapaio.graphics.Plotter.hist;
import static rapaio.sys.WS.draw;

/**
 * This is a tool which enables one to inspect a large data set feature by feature
 * for feature filtering and reporting.
 * <p>
 * Created by <a href="mailto:tutuianu@amazon.com">Aurelian Tutuianu</a> on 9/21/15.
 */
public class CsvFeatureCleaning {

    private String idVar;
    private Set<String> ignoredVars = new HashSet<>();
    private Set<String> targetVars = new HashSet<>();
    private Map<String, Integer> indexes;
    private Map<VarType, List<BiConsumer<Var, Frame>>> reports = new HashMap<>();

    private final String fileName;

    public CsvFeatureCleaning(String fileName) {
        this.fileName = fileName;

        for (VarType type : VarType.values()) {
            reports.put(type, new ArrayList<>());
            reports.get(type).add((v, df) -> v.printSummary());
        }

        addReports();
    }

    public String getIdVar() {
        return idVar;
    }

    public void setIdVar(String idVar) {
        this.idVar = idVar;
    }

    public Set<String> getIgnoredVars() {
        return ignoredVars;
    }

    public void setIgnoredVars(String... ignoredVars) {
        this.ignoredVars = new HashSet<>();
        Collections.addAll(this.ignoredVars, ignoredVars);
    }

    public Set<String> getTargetVars() {
        return targetVars;
    }

    public void setTargetVars(String... targetVars) {
        this.targetVars = new HashSet<>();
        Collections.addAll(this.targetVars, targetVars);
    }

    public void runReport() throws IOException {
        String[] naValues = new String[]{"NA", "-1", "", "[]", "-99999"};
        Frame names = new Csv()
                .withQuotes(true)
                .withNAValues(naValues)
                .withTypes(VarType.NOMINAL, "target")
                .withEndRow(1000)
                .read(fileName);

        indexes = new HashMap<>();
        String[] varNames = names.varNames();
        for (int i = 0; i < varNames.length; i++) {
            indexes.put(varNames[i], i);
        }

        Set<Integer> targetIndexes = targetVars.stream().map(indexes::get).collect(toSet());
        Frame targets = new Csv()
                .withQuotes(true)
                .withNAValues(naValues)
                .withTypes(VarType.NOMINAL, "target")
                .withSkipCols(n -> !targetIndexes.contains(n))
                .read(new FileInputStream(new File(fileName)));

        Var pos = Filters.shuffle(Index.newSeq(varNames.length));

        for (int i = 0; i < pos.rowCount(); i++) {
            String varName = varNames[pos.index(i)];
            if (ignoredVars.contains(varName))
                continue;
            if (targetVars.contains(varName))
                continue;

            Predicate<Integer> keepFilter = n -> Objects.equals(n, indexes.get(varName));
            Frame df = new Csv()
                    .withQuotes(true)
                    .withNAValues(naValues)
                    .withTypes(VarType.NOMINAL, "target")
                    .withSkipCols(n -> keepFilter.negate().test(n))
                    .read(new FileInputStream(new File(fileName)));

            for (BiConsumer<Var, Frame> consumer : reports.get(df.var(varName).type())) {
                consumer.accept(df.var(varName), targets);
            }
        }
    }

    private void addReports() {
        reports.get(VarType.NUMERIC).add((v, df) -> {

            long distinct = v.stream().mapToDouble().distinct().limit(10_000).count();
            WS.println("distinct count: " + distinct + "\n");

            draw(hist(v));
        });
        reports.get(VarType.INDEX).add((v, df) -> {

            long distinct = v.stream().mapToInt().distinct().limit(10_000).count();
            WS.println("distinct count: " + distinct + "\n");
            draw(hist(v));
        });
    }


    public static void main(String[] args) throws IOException {
        CsvFeatureCleaning cleaner = new CsvFeatureCleaning("/home/ati/data/springleaf/train.csv");
        cleaner.setIdVar("ID");
        cleaner.setTargetVars("target");


//        final Set<Integer> skipColsSet = Arrays.asList(
//                8, 9, 10, 11, 12,
//                18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
//                38, 39, 40, 41, 42, 43, 44,
//                188, 189, 190, 196, 197, 199,
//                202, 203, 207, 213, 215, 216, 221, 222, 223, 229, 239, 246,
//
//                 time base
//                73, 75, 155, 156, 157, 158, 159, 160, 161, 162, 163, 166, 167, 168, 169, 176, 177, 178, 179,
//                204, 217,
//                 social sec number
//                214,
//                 zip code ?
//                212
//        ).stream()
//                .map(i -> indexes.get(String.format("VAR_%04d", i)))
//                .collect(toSet());

        String[] ignoredVars = new String[]{
                "ID"
        };

        cleaner.setIgnoredVars(ignoredVars);

        cleaner.runReport();


    }
}

