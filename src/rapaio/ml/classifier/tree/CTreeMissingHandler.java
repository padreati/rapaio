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

package rapaio.ml.classifier.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;
import rapaio.util.Tag;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeMissingHandler extends Serializable {

    Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, CTree.Candidate candidate);

    Tag<CTreeMissingHandler> Ignored = Tag.valueOf("Ignored", (Frame df, Var weights, CTree.Candidate candidate) -> {

        List<SPredicate<FSpot>> p = candidate.getGroupPredicates();
        List<Mapping> mappings = IntStream.range(0, p.size()).boxed().map(i -> Mapping.newEmpty()).collect(toList());

        df.stream().forEach(s -> {
            for (int i = 0; i < p.size(); i++) {
                if (p.get(i).test(s)) {
                    mappings.get(i).add(s.row());
                    break;
                }
            }
        });
        return new Pair<>(
                mappings.stream().map(df::mapRows).collect(toList()),
                mappings.stream().map(weights::mapRows).collect(toList())
        );
    });


    Tag<CTreeMissingHandler> ToMajority = Tag.valueOf("ToMajority", (Frame df, Var weights, CTree.Candidate candidate) -> {

        List<SPredicate<FSpot>> p = candidate.getGroupPredicates();
        List<Mapping> mappings = IntStream.range(0, p.size()).boxed().map(i -> Mapping.newEmpty()).collect(toList());

        List<Integer> missingSpots = new LinkedList<>();
        df.stream().forEach(s -> {
            for (int i = 0; i < p.size(); i++) {
                if (p.get(i).test(s)) {
                    mappings.get(i).add(s.row());
                    return;
                }
            }
            missingSpots.add(s.row());
        });
        List<Integer> lens = mappings.stream().map(Mapping::size).collect(toList());
        Collections.shuffle(lens);
        int majorityGroup = 0;
        int majoritySize = 0;
        for (int i = 0; i < mappings.size(); i++) {
            if (mappings.get(i).size() > majoritySize) {
                majorityGroup = i;
                majoritySize = mappings.get(i).size();
            }
        }
        final int index = majorityGroup;

        mappings.get(index).addAll(missingSpots);

        return new Pair<>(
                mappings.stream().map(df::mapRows).collect(toList()),
                mappings.stream().map(weights::mapRows).collect(toList())
        );
    });

    Tag<CTreeMissingHandler> ToAllWeighted = Tag.valueOf("ToAllWeighted", (Frame df, Var weights, CTree.Candidate candidate) -> {

        List<SPredicate<FSpot>> pred = candidate.getGroupPredicates();
        List<Mapping> mappings = IntStream.range(0, pred.size()).boxed().map(i -> Mapping.newEmpty()).collect(toList());

        List<Integer> missingSpots = new ArrayList<>();
        df.stream().forEach(s -> {
            for (int i = 0; i < pred.size(); i++) {
                if (pred.get(i).test(s)) {
                    mappings.get(i).add(s.row());
                    return;
                }
            }
            missingSpots.add(s.row());
        });

        final double[] p = new double[mappings.size()];
        double n = 0;
        for (int i = 0; i < mappings.size(); i++) {
            p[i] = mappings.get(i).size();
            n += p[i];
        }
        for (int i = 0; i < p.length; i++) {
            p[i] /= n;
        }
        List<Var> weightsList = mappings.stream().map(weights::mapRows).map(Var::solidCopy).collect(toList());
        for (int i = 0; i < mappings.size(); i++) {
            final int ii = i;
            missingSpots.forEach(row -> {
                mappings.get(ii).add(row);
                weightsList.get(ii).addValue(weights.missing(row) ? p[ii] : weights.value(row) * p[ii]);
            });
        }
        List<Frame> frames = mappings.stream().map(df::mapRows).collect(toList());
        return new Pair<>(frames, weightsList);
    });

    Tag<CTreeMissingHandler> ToRandom = Tag.valueOf("ToRandom", (Frame df, Var weights, CTree.Candidate candidate) -> {

        List<SPredicate<FSpot>> pred = candidate.getGroupPredicates();
        List<Mapping> mappings = IntStream.range(0, pred.size()).boxed().map(i -> Mapping.newEmpty()).collect(toList());

        final Set<Integer> missingSpots = new HashSet<>();
        df.stream().forEach(s -> {
            for (int i = 0; i < pred.size(); i++) {
                if (pred.get(i).test(s)) {
                    mappings.get(i).add(s.row());
                    return;
                }
            }
            missingSpots.add(s.row());
        });
        missingSpots.forEach(rowId -> mappings.get(RandomSource.nextInt(mappings.size())).add(rowId));
        List<Frame> frameList = mappings.stream().map(df::mapRows).collect(toList());
        List<Var> weightList = mappings.stream().map(weights::mapRows).collect(toList());
        return new Pair<>(frameList, weightList);
    });
}

