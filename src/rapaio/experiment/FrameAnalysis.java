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

package rapaio.experiment;

import rapaio.core.tools.DVector;
import rapaio.data.*;
import rapaio.data.stream.VSpot;
import rapaio.io.Csv;
import rapaio.sys.WS;

import java.io.IOException;
import java.util.*;

public class FrameAnalysis {

    private final int chunkSize = 50;
    private final int bins = 256;
    private final int maxVars = Integer.MAX_VALUE;
    private final int targetIndex = 1933;

    public Frame buildCsvFrame(Csv csv, String file) throws IOException {

        String[] varNames = csv.withEndRow(1000).read(file).varNames();
        csv.withEndRow(Integer.MAX_VALUE);
        int start = 0;

        NomVar name = NomVar.empty().withName("name");
        NomVar type = NomVar.empty().withName("type");
        IdxVar count = IdxVar.empty().withName("count");
        IdxVar missing = IdxVar.empty().withName("missing");
        List<Var> h1 = new ArrayList<>();
        List<Var> h2 = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            h1.add(NumVar.empty().withName("h1_" + i));
            h2.add(NumVar.empty().withName("h2_" + i));
        }

        Var target = csv.withSkipCols(n -> n != targetIndex).read(file).var(0);

        while (start < varNames.length && start < maxVars) {
            int pos = start;
            csv.withSkipCols(n -> !((n >= pos && n < pos + chunkSize && n < varNames.length && n < maxVars)));
            Frame vs = csv.read(file);

            vs.varStream().forEach(var -> {
                WS.println(var.name());
                name.addLabel(var.name());
                type.addLabel(var.type().name());
                int countValue;
                switch (var.type()) {
                    case NOMINAL:
                    case ORDINAL:
                        countValue = var.levels().length;
                        break;
                    case INDEX:
                    case BINARY:
                        countValue = (int) var.stream().mapToInt().distinct().count();
                        break;
                    case STAMP:
                        countValue = (int) var.stream().mapToLong(VSpot::stamp).distinct().count();
                        break;
                    case NUMERIC:
                        countValue = (int) var.stream().mapToDouble().distinct().count();
                        break;
                    default:
                        countValue = (int) var.stream().mapToString().distinct().count();
                }
                count.addIndex(countValue);
                missing.addIndex((int) var.stream().incomplete().count());

                double[] h1v = new double[bins];
                double[] h2v = new double[bins];
                double[][] h = new double[][]{h1v, h2v};
                switch (var.type()) {
                    case BINARY:
                        var.stream().complete().forEach(s -> h[target.index(s.row()) - 1][s.index()]++);
                        break;
                    case INDEX:
                    case NUMERIC:
                    case ORDINAL:
                        double min = var.stream().complete().mapToDouble().min().getAsDouble();
                        double max = var.stream().complete().mapToDouble().max().getAsDouble();
                        double step = (max - min) / bins;
                        var.stream().complete().forEach(s -> {
                            int bin = (int) Math.floor((s.value() - min) / step);
                            if (bin == bins)
                                bin--;
                            h[target.index(s.row()) - 1][bin]++;
                        });
                        break;
                    case STAMP:
                        long min2 = var.stream().complete().mapToLong(VSpot::stamp).min().getAsLong();
                        long max2 = var.stream().complete().mapToLong(VSpot::stamp).max().getAsLong();
                        long step2 = (max2 - min2) / bins;
                        var.stream().complete().forEach(s -> {
                            int bin = (int) Math.floor((s.value() - min2) / step2);
                            if (bin == bins)
                                bin--;
                            h[target.index(s.row()) - 1][bin]++;
                        });
                        break;
                    case NOMINAL:
                        DVector dv1 = DVector.fromCount(false, var.stream().complete().filter(s -> target.index(s.row()) == 1).toMappedVar());
                        double[] v1 = dv1.streamValues().skip(1).sorted().toArray();
                        for (int i = 0; i < v1.length; i++) {
                            h[0][i < bins ? i : bins - 1] += v1[i];
                        }
                        DVector dv2 = DVector.fromCount(false, var.stream().complete().filter(s -> target.index(s.row()) == 2).toMappedVar());
                        double[] v2 = dv2.streamValues().skip(1).sorted().toArray();
                        for (int i = 0; i < v1.length; i++) {
                            h[1][i < bins ? i : bins - 1] += v2[i];
                        }
                        break;
                    default:
                        HashMap<String, Integer> counts = new HashMap<>();
                        var.stream().filter(s -> target.index(s.row()) == 1).mapToString().forEach(txt -> {
                            if (!counts.containsKey(txt))
                                counts.put(txt, 0);
                            counts.put(txt, counts.get(txt) + 1);
                        });
                        TreeMap<Integer, List<String>> reverse = new TreeMap<>();
                        counts.entrySet().forEach(e -> {
                            if (!reverse.containsKey(e.getValue()))
                                reverse.put(e.getValue(), new ArrayList<>());
                            reverse.get(e.getValue()).add(e.getKey());
                        });
                        int p = 0;
                        for (Map.Entry<Integer, List<String>> e : reverse.entrySet()) {
                            for (String key : e.getValue()) {
                                h[0][p < bins ? p++ : bins - 1] += e.getKey();
                            }
                        }

                        HashMap<String, Integer> counts2 = new HashMap<>();
                        var.stream().filter(s -> target.index(s.row()) == 1).mapToString().forEach(txt -> {
                            if (!counts2.containsKey(txt))
                                counts2.put(txt, 0);
                            counts2.put(txt, counts2.get(txt) + 1);
                        });
                        TreeMap<Integer, List<String>> reverse2 = new TreeMap<>();
                        counts2.entrySet().forEach(e -> {
                            if (!reverse2.containsKey(e.getValue()))
                                reverse2.put(e.getValue(), new ArrayList<>());
                            reverse2.get(e.getValue()).add(e.getKey());
                        });
                        int p2 = 0;
                        for (Map.Entry<Integer, List<String>> e : reverse2.entrySet()) {
                            for (String key : e.getValue()) {
                                h[1][p2 < bins ? p2++ : bins - 1] += e.getKey();
                            }
                        }
                }
                for (int i = 0; i < bins; i++) {
                    h1.get(i).addValue(h1v[i]);
                    h2.get(i).addValue(h2v[i]);
                }
            });

            // next chunk
            start += chunkSize;
        }

        List<Var> vars = new ArrayList<>();
        vars.add(name);
        vars.add(type);
        vars.add(count);
        vars.add(missing);
        vars.addAll(h1);
        vars.addAll(h2);
        return SolidFrame.byVars(vars);
    }

}
