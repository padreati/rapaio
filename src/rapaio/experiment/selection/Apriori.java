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

package rapaio.experiment.selection;

import rapaio.core.tools.DVector;
import rapaio.data.*;
import rapaio.printer.Printable;
import rapaio.sys.WS;
import rapaio.util.Pair;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/23/15.
 */
public class Apriori implements Printable {

    private Var targetVar;
    private Frame inputDf;
    private BiPredicate<Integer, DVector> filter;

    private List<List<Pair<AprioriRule, DVector>>> P;
    private List<AprioriRule> rules;
    private double coverage;

    public String[] inputVarNames() {
        return inputDf.getVarNames();
    }

    public void train(Frame df, String target, BiPredicate<Integer, DVector> filter) {

        List<Var> inputVars = df.varStream()
                .filter(var -> var.getType().equals(VarType.NOMINAL))
                .filter(var -> !var.getName().equals(target))
                .collect(Collectors.toList());
        this.inputDf = SolidFrame.byVars(inputVars);
        this.targetVar = df.getVar(target);
        this.filter = filter;

        List<AprioriRule> C = new ArrayList<>();
        P = new ArrayList<>();

        // build dictionary of rules $C0$

        for (int i = 0; i < inputDf.getVarCount(); i++) {
            Var input = inputDf.getVar(i);
            for (String level : input.getLevels()) {
                AprioriRuleClause clause = new AprioriRuleClause(input.getName(), level);
                AprioriRule rule = new AprioriRule();
                rule.addClause(clause);
                C.add(rule);
            }
        }

        List<Pair<AprioriRule, DVector>> counts = C.stream().map(rule -> Pair.from(rule,
                DVector.empty(false, targetVar.getLevels())))
                .collect(Collectors.toList());

        for (int i = 0; i < df.getRowCount(); i++) {
            for (Pair<AprioriRule, DVector> cnt : counts) {
                if (cnt._1.matchRow(df, i)) {
                    cnt._2.increment(targetVar.getIndex(i), 1);
                }
            }
        }

        List<Pair<AprioriRule, DVector>> list = counts.stream()
                .filter(pair -> filter.test(df.getRowCount(), pair._2))
                .collect(Collectors.toList());
        list.sort((o1, o2) -> -Double.compare(o1._2.sum(), o2._2.sum()));
        P.add(list);

        // do iterations

        List<AprioriRule> base = P.get(0).stream().map(pair -> pair._1).collect(Collectors.toList());

        while (true) {
            int k = P.size();

            Map<String, Pair<AprioriRule, DVector>> cnts = new HashMap<>();

            // loop for all possibilities
            for (int i = 0; i < df.getRowCount(); i++) {
                for (AprioriRule b : base) {
                    if (!b.matchRow(df, i))
                        continue;
                    for (Pair<AprioriRule, DVector> tPrev : P.get(k - 1)) {
                        if (!tPrev._1.isExtention(b))
                            continue;
                        if (!tPrev._1.matchRow(df, i))
                            continue;
                        AprioriRule next = tPrev._1.extend(b);
                        if (!cnts.containsKey(next.toString())) {
                            cnts.put(next.toString(), Pair.from(next, DVector.empty(false, targetVar.getLevels())));
                        }
                        cnts.get(next.toString())._2.increment(targetVar.getIndex(i), 1);
                    }
                }
            }

            // keep only survivors
            List<Pair<AprioriRule, DVector>> top = cnts.values().stream()
                    .filter(pair -> filter.test(df.getRowCount(), pair._2))
                    .collect(Collectors.toList());

            if (top.isEmpty()) {
                break;
            }
            top.sort((o1, o2) -> -Double.compare(o1._2.sum(), o2._2.sum()));
            P.add(top);
        }

        // eliminate redundant tasks
        for (int i = 0; i < P.size() - 1; i++) {
            Iterator<Pair<AprioriRule, DVector>> it = P.get(i).iterator();
            while (it.hasNext()) {
                Pair<AprioriRule, DVector> next = it.next();
                boolean out = false;
                for (int j = i + 1; j < P.size(); j++) {
                    for (Pair<AprioriRule, DVector> pair : P.get(j)) {
                        if (pair._1.contains(next._1)) {
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
        for (List<Pair<AprioriRule, DVector>> aP : P) {
            rules.addAll(aP.stream().map(pair -> pair._1).collect(Collectors.toSet()));
        }

        // create coverage
        double count = 0;
        for (int i = 0; i < df.getRowCount(); i++) {
            for (int j = 0; j < rules.size(); j++) {
                if (rules.get(j).matchRow(df, i)) {
                    count++;
                    break;
                }
            }
        }
        coverage = count / df.getRowCount();
    }

    @Override
    public String getSummary() {

        StringBuilder sb = new StringBuilder();
        // print a list of rules

        sb.append("# Apriori\n");

        for (int i = 0; i < P.size(); i++) {
            sb.append("## rules of size: ").append(i + 1).append("\n");
            for (int j = 0; j < P.get(i).size(); j++) {
                sb.append(j + 1).append(". ").append(P.get(i).get(j)._1.toString())
                        .append(" ")
                        .append(WS.formatFlex(P.get(i).get(j)._2.sum())).append(" [");
                for (int k = 1; k < targetVar.getLevels().length; k++) {
                    sb.append(WS.formatShort(P.get(i).get(j)._2.get(k))).append(",");
                }
                sb.append("]\n");
            }
        }
        sb.append("\n");
        sb.append("Rules: ").append(rules.size()).append("\n");
        sb.append("Coverage: ").append(WS.formatFlex(coverage)).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    public Frame buildFeatures(Frame df) {
        List<Var> vars = rules.stream().map(r -> NominalVar.empty(df.getRowCount(), "?", "1", "0")).collect(Collectors.toList());
        for (int i = 0; i < vars.size(); i++) {
            vars.get(i).withName("Apriori_" + (i + 1));
        }
        for (int i = 0; i < df.getRowCount(); i++) {
            for (int j = 0; j < rules.size(); j++) {
                vars.get(j).setIndex(i, rules.get(j).matchRow(df, i) ? 1 : 2);
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
        Set<AprioriRuleClause> set = clauses.stream().collect(Collectors.toSet());
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