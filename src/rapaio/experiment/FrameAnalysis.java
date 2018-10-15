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

package rapaio.experiment;

import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.stream.VSpot;
import rapaio.io.Csv;
import rapaio.sys.WS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FrameAnalysis {

    private final int chunkSize = 50;
    private final int bins = 256;
    private final int maxVars = Integer.MAX_VALUE;
    private final int targetIndex = 1933;

    public Frame buildCsvFrame(Csv csv, String file) throws IOException {

        String[] varNames = csv.withEndRow(1000).read(file).varNames();
        csv.withEndRow(Integer.MAX_VALUE);
        int start = 0;

        VarNominal name = VarNominal.empty().withName("name");
        VarNominal type = VarNominal.empty().withName("type");
        VarInt count = VarInt.empty().withName("count");
        VarInt missing = VarInt.empty().withName("missing");
        List<Var> h1 = new ArrayList<>();
        List<Var> h2 = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            h1.add(VarDouble.empty().withName("h1_" + i));
            h2.add(VarDouble.empty().withName("h2_" + i));
        }

        Var target = csv.withSkipCols(n -> n != targetIndex).read(file).rvar(0);

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
                        countValue = var.levels().size();
                        break;
                    case INT:
                    case BINARY:
                        countValue = (int) var.stream().mapToInt().distinct().count();
                        break;
                    case LONG:
                        countValue = (int) var.stream().mapToLong(VSpot::getLong).distinct().count();
                        break;
                    case DOUBLE:
                        countValue = (int) var.stream().mapToDouble().distinct().count();
                        break;
                    default:
                        countValue = (int) var.stream().mapToString().distinct().count();
                }
                count.addInt(countValue);
                missing.addInt((int) var.stream().incomplete().count());

                double[] h1v = new double[bins];
                double[] h2v = new double[bins];
                double[][] h = new double[][]{h1v, h2v};
                switch (var.type()) {
                    case BINARY:
                        var.stream().complete().forEach(s -> h[target.getInt(s.row()) - 1][s.getInt()]++);
                        break;
                    case INT:
                    case DOUBLE:
                        double min = var.stream().complete().mapToDouble().min().getAsDouble();
                        double max = var.stream().complete().mapToDouble().max().getAsDouble();
                        double step = (max - min) / bins;
                        var.stream().complete().forEach(s -> {
                            int bin = (int) Math.floor((s.getDouble() - min) / step);
                            if (bin == bins)
                                bin--;
                            h[target.getInt(s.row()) - 1][bin]++;
                        });
                        break;
                    case LONG:
                        long min2 = var.stream().complete().mapToLong(VSpot::getLong).min().getAsLong();
                        long max2 = var.stream().complete().mapToLong(VSpot::getLong).max().getAsLong();
                        long step2 = (max2 - min2) / bins;
                        var.stream().complete().forEach(s -> {
                            int bin = (int) Math.floor((s.getDouble() - min2) / step2);
                            if (bin == bins)
                                bin--;
                            h[target.getInt(s.row()) - 1][bin]++;
                        });
                        break;
                    case NOMINAL:
                        DVector dv1 = DVector.fromCounts(false, var.stream().complete().filter(s -> target.getInt(s.row()) == 1).toMappedVar());
                        double[] v1 = dv1.streamValues().sorted().toArray();
                        for (int i = 0; i < v1.length; i++) {
                            h[0][i < bins ? i : bins - 1] += v1[i];
                        }
                        DVector dv2 = DVector.fromCounts(false, var.stream().complete().filter(s -> target.getInt(s.row()) == 2).toMappedVar());
                        double[] v2 = dv2.streamValues().sorted().toArray();
                        for (int i = 0; i < v1.length; i++) {
                            h[1][i < bins ? i : bins - 1] += v2[i];
                        }
                        break;
                    default:
                        HashMap<String, Integer> counts = new HashMap<>();
                        var.stream().filter(s -> target.getInt(s.row()) == 1).mapToString().forEach(txt -> {
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
                        var.stream().filter(s -> target.getInt(s.row()) == 1).mapToString().forEach(txt -> {
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
                    h1.get(i).addDouble(h1v[i]);
                    h2.get(i).addDouble(h2v[i]);
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
