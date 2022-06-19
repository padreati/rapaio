/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.Pair;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/23/15.
 */
public class Apriori implements Printable {

    private Frame inputDf;

    private List<List<Pair<AprioriRule, DensityVector<String>>>> P;
    private List<AprioriRule> rules;
    private List<String> targetLevels;
    private double coverage;

    public String[] inputVarNames() {
        return inputDf.varNames();
    }

    public void train(Frame df, String target, BiPredicate<Integer, DensityVector<String>> filter) {

        List<Var> inputVars = df.varStream()
                .filter(var -> var.type().equals(VarType.NOMINAL))
                .filter(var -> !var.name().equals(target))
                .collect(Collectors.toList());
        this.inputDf = SolidFrame.byVars(inputVars);
        this.targetLevels = df.levels(target);

        List<AprioriRule> C = new ArrayList<>();
        P = new ArrayList<>();

        // build dictionary of rules $C0$

        for (int i = 0; i < inputDf.varCount(); i++) {
            Var input = inputDf.rvar(i);
            for (String level : input.levels()) {
                AprioriRuleClause clause = new AprioriRuleClause(input.name(), level);
                AprioriRule rule = new AprioriRule();
                rule.addClause(clause);
                C.add(rule);
            }
        }

        List<Pair<AprioriRule, DensityVector<String>>> counts = C.stream().map(rule -> Pair.from(rule,
                DensityVector.emptyByLabels(false, df.levels(target))))
                .collect(Collectors.toList());

        for (int i = 0; i < df.rowCount(); i++) {
            for (Pair<AprioriRule, DensityVector<String>> cnt : counts) {
                if (cnt.v1.matchRow(df, i)) {
                    cnt.v2.increment(df.getLabel(i, target), 1);
                }
            }
        }

        P.add(counts.stream()
                .filter(pair -> filter.test(df.rowCount(), pair.v2))
                .sorted((o1, o2) -> -Double.compare(o1.v2.sum(), o2.v2.sum()))
                .collect(Collectors.toList()));

        // do iterations

        List<AprioriRule> base = P.get(0).stream().map(pair -> pair.v1).collect(Collectors.toList());

        while (true) {
            int k = P.size();

            Map<String, Pair<AprioriRule, DensityVector<String>>> cnts = new HashMap<>();

            // loop for all possibilities
            for (int i = 0; i < df.rowCount(); i++) {
                for (AprioriRule b : base) {
                    if (!b.matchRow(df, i))
                        continue;
                    for (Pair<AprioriRule, DensityVector<String>> tPrev : P.get(k - 1)) {
                        if (!tPrev.v1.isExtention(b))
                            continue;
                        if (!tPrev.v1.matchRow(df, i))
                            continue;
                        AprioriRule next = tPrev.v1.extend(b);
                        if (!cnts.containsKey(next.toString())) {
                            cnts.put(next.toString(), Pair.from(next, DensityVector.emptyByLabels(false, df.levels(target))));
                        }
                        cnts.get(next.toString()).v2.increment(df.getInt(i, target), 1);
                    }
                }
            }

            // keep only survivors
            List<Pair<AprioriRule, DensityVector<String>>> top = cnts.values().stream()
                    .filter(pair -> filter.test(df.rowCount(), pair.v2))
                    .collect(Collectors.toList());

            if (top.isEmpty()) {
                break;
            }
            top.sort((o1, o2) -> -Double.compare(o1.v2.sum(), o2.v2.sum()));
            P.add(top);
        }

        // eliminate redundant tasks
        for (int i = 0; i < P.size() - 1; i++) {
            Iterator<Pair<AprioriRule, DensityVector<String>>> it = P.get(i).iterator();
            while (it.hasNext()) {
                Pair<AprioriRule, DensityVector<String>> next = it.next();
                boolean out = false;
                for (int j = i + 1; j < P.size(); j++) {
                    for (Pair<AprioriRule, DensityVector<String>> pair : P.get(j)) {
                        if (pair.v1.contains(next.v1)) {
                            out = true;
                            break;
                        }
                    }
                    if (out)
                        break;
                }
                if (out)
                    it.remove();
            }
        }

        // create final rules

        rules = new ArrayList<>();
        for (List<Pair<AprioriRule, DensityVector<String>>> aP : P) {
            rules.addAll(aP.stream().map(pair -> pair.v1).collect(Collectors.toSet()));
        }

        // create coverage
        double count = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            for (AprioriRule rule : rules) {
                if (rule.matchRow(df, i)) {
                    count++;
                    break;
                }
            }
        }
        coverage = count / df.rowCount();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {

        StringBuilder sb = new StringBuilder();
        // print a list of rules

        sb.append("# Apriori\n");

        for (int i = 0; i < P.size(); i++) {
            sb.append("## rules of size: ").append(i + 1).append("\n");
            for (int j = 0; j < P.get(i).size(); j++) {
                sb.append(j + 1).append(". ").append(P.get(i).get(j).v1.toString())
                        .append(" ")
                        .append(Format.floatFlex(P.get(i).get(j).v2.sum())).append(" [");
                for (int k = 1; k < targetLevels.size(); k++) {
                    sb.append(Format.floatShort(P.get(i).get(j).v2.get(k))).append(",");
                }
                sb.append("]\n");
            }
        }
        sb.append("\n");
        sb.append("Rules: ").append(rules.size()).append("\n");
        sb.append("Coverage: ").append(Format.floatFlex(coverage)).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    public Frame buildFeatures(Frame df) {
        List<Var> vars = rules.stream().map(r -> VarNominal.empty(df.rowCount(), "?", "1", "0")).collect(Collectors.toList());
        for (int i = 0; i < vars.size(); i++) {
            vars.get(i).name("Apriori_" + (i + 1));
        }
        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < rules.size(); j++) {
                vars.get(j).setInt(i, rules.get(j).matchRow(df, i) ? 1 : 2);
            }
        }
        return SolidFrame.byVars(vars);
    }
}

class AprioriRule {
    public final List<AprioriRuleClause> clauses = new ArrayList<>();

    public void addClause(AprioriRuleClause clause) {
        clauses.add(clause);
    }

    public boolean matchRow(Frame df, int row) {
        for (AprioriRuleClause clause : clauses) {
            if (!df.getLabel(row, clause.varName).equals(clause.level))
                return false;
        }
        return true;
    }

    public boolean isExtention(AprioriRule rule) {
        return rule.clauses.size() == 1 &&
                rule.clauses.get(0).full.compareTo(clauses.get(clauses.size() - 1).full) > 0;
    }

    public AprioriRule extend(AprioriRule rule) {
        if (rule.clauses.size() != 1)
            return null;
        AprioriRule next = new AprioriRule();
        for (AprioriRuleClause c : clauses)
            next.addClause(c);
        next.addClause(rule.clauses.get(0));
        return next;
    }

    public boolean contains(AprioriRule rule) {
        Set<AprioriRuleClause> set = new HashSet<>(clauses);
        for (AprioriRuleClause c : rule.clauses) {
            if (!set.contains(c))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return clauses.stream().map(AprioriRuleClause::toString).collect(Collectors.joining(", "));
    }
}

class AprioriRuleClause {

    public final String varName;
    public final String level;
    public final String full;

    public AprioriRuleClause(String varName, String level) {
        this.varName = varName;
        this.level = level;
        this.full = varName + ":" + level;
    }

    public String toString() {
        return full;
    }
}