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

package rapaio.ml.classifier.meta;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.eval.Confusion;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/15.
 */
public class CStepwiseSelection extends AbstractClassifier {

    private static final long serialVersionUID = 2642562123626893974L;

    private Classifier c;
    private int minVars = 1;
    private int maxVars = 1;
    private String[] startSelection = new String[]{};
    private int restartAfter = 2;
    private int maxSearch = Integer.MAX_VALUE;
    private Frame test;

    // training artifacts
    private List<String> selection;
    Classifier best;

    @Override
    public String name() {
        return "CStepwiseSelection";
    }

    @Override
    public String fullName() {
        return null;
    }

    @Override
    public Classifier newInstance() {
        return new CStepwiseSelection()
                .withRestartAfter(restartAfter)
                .withClassifier(c)
                .withMaxVars(maxVars)
                .withMinVars(minVars)
                .withStartSelection(startSelection)
                .withTestFrame(test)
                .withPoolSize(poolSize());
    }

    @Override
    public Capabilities capabilities() {
        return c.capabilities();
    }

    public CStepwiseSelection withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    public CStepwiseSelection withMinVars(int minVars) {
        this.minVars = minVars;
        return this;
    }

    public CStepwiseSelection withMaxVars(int maxVars) {
        this.maxVars = maxVars;
        return this;
    }

    public CStepwiseSelection withStartSelection(String... startSelection) {
        this.startSelection = startSelection;
        return this;
    }

    public CStepwiseSelection withRestartAfter(int restartAfter) {
        this.restartAfter = restartAfter;
        return this;
    }

    public CStepwiseSelection withMaxSearch(int maxSearch) {
        this.maxSearch = maxSearch;
        return this;
    }

    public CStepwiseSelection withTestFrame(Frame test) {
        this.test = test;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        selection = new VarRange(startSelection).parseVarNames(df);
        Frame testFrame = test != null ? test : df;

        List<String> bestSelection = new ArrayList<>(selection);
        Classifier bestClassifier = null;
        double bestAcc = 0.0;
        String forwardNext = null;
        String backwardNext = null;

        for (int r = 0; r < runs(); r++) {
            boolean found = false;

            Set<String> inSet = new HashSet<>(selection);
            if (selection.size() < maxVars) {
                // do forward selection
                List<String> in = Arrays.stream(inputNames()).collect(Collectors.toList());
                Collections.shuffle(in);
                int restart = 0;
                for (int i = 0; i < in.size() && i < maxSearch; i++) {
                    String test = in.get(i);
                    if (inSet.contains(test))
                        continue;

                    List<String> next = new ArrayList<>(selection);
                    next.add(test);
                    next.add(firstTargetName());

                    Classifier cNext = c.newInstance();
                    cNext.train(df.mapVars(next), firstTargetName());
                    Confusion cm = new Confusion(testFrame.var(firstTargetName()), cNext.fit(testFrame).firstClasses());

                    double acc = cm.accuracy();
                    if (acc > bestAcc) {

                        WS.println(WS.formatFlex(acc));
                        bestAcc = acc;
                        bestClassifier = cNext;
                        forwardNext = test;
                        backwardNext = null;
                        found = true;
                        restart++;
                        if (restart >= restartAfter) {
                            break;
                        }
                    }
                }
            }
            if (!found && selection.size() > minVars) {
                // do backward selection
                int restart = 0;
                Collections.shuffle(selection);
                for (int i = 0; i < selection.size() && i < maxSearch; i++) {

                    String test = selection.get(i);
                    List<String> next = selection.stream().filter(n -> !test.equals(n)).collect(toList());
                    next.add(firstTargetName());

                    Classifier cNext = c.newInstance();
                    cNext.train(df.mapVars(next), firstTargetName());
                    Confusion cm = new Confusion(testFrame.var(firstTargetName()), cNext.fit(testFrame).firstClasses());

                    double acc = cm.accuracy();

                    if (acc > bestAcc) {
                        WS.println(WS.formatFlex(acc));
                        bestAcc = acc;
                        bestClassifier = cNext;
                        forwardNext = null;
                        backwardNext = test;
                        found = true;
                        restart++;
                        if (restart >= restartAfter)
                            break;
                    }
                }
            }
            if (!found)
                break;

            best = bestClassifier;

            String testNext = (forwardNext == null) ? backwardNext : forwardNext;
            if (forwardNext != null) {
                selection.add(testNext);
            }
            if (backwardNext != null) {
                selection = selection.stream().filter(n -> !n.equals(testNext)).collect(toList());
            }

            WS.println("best selection: ");
            TextTable tt = TextTable.newEmpty(selection.size() + 1, 2);
            tt.set(0, 0, "No.", 0);
            tt.set(0, 1, "Name", -1);
            for (int i = 0; i < selection.size(); i++) {
                tt.set(i + 1, 0, i + ".", 1);
                tt.set(i + 1, 1, selection.get(i), -1);
            }
            tt.withMerge();

            tt.withHeaderRows(1);
            tt.printSummary();

            WS.println("last test: " + testNext);

            new Confusion(testFrame.var(firstTargetName()), best.fit(testFrame).firstClasses()).printSummary();
        }


        return true;
    }

    @Override
    protected CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        return best.fit(df, withClasses, withDistributions);
    }

}
