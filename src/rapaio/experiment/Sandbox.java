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

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.filter.FFilter;
import rapaio.data.filter.var.VTransformBoxCox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/17.
 */
public class Sandbox {

    public static void main(String[] args) {


        VarDouble x = VarDouble.seq(0, 1000).withName("x");
        Var y = x.solidCopy().fapply(VTransformBoxCox.with(0.1)).withName("y");

        BoundFrame.byVars(x, y).printLines(100);
    }
}

class TitleFilter implements FFilter {
    private HashMap<String, String[]> replaceMap = new HashMap<>();
    private Function<String, String> titleFun = txt -> {
        for (Map.Entry<String, String[]> e : replaceMap.entrySet()) {
            for (int i = 0; i < e.getValue().length; i++) {
                if (txt.contains(" " + e.getValue()[i] + ". "))
                    return e.getKey();
            }
        }
        return "?";
    };

    @Override
    public void fit(Frame df) {
        replaceMap.put("Mrs", new String[]{"Mrs", "Mme", "Lady", "Countess"});
        replaceMap.put("Mr", new String[]{"Mr", "Sir", "Don", "Ms"});
        replaceMap.put("Miss", new String[]{"Miss", "Mlle"});
        replaceMap.put("Master", new String[]{"Master"});
        replaceMap.put("Dr", new String[]{"Dr"});
        replaceMap.put("Military", new String[]{"Col", "Major", "Jonkheer", "Capt"});
        replaceMap.put("Rev", new String[]{"Rev"});
    }

    @Override
    public Frame apply(Frame df) {
        VarNominal title = VarNominal.empty(0, new ArrayList<>(replaceMap.keySet())).withName("Title");
        df.rvar("Name").stream().mapToString().forEach(name -> title.addLabel(titleFun.apply(name)));
        return df.bindVars(title);
    }

    @Override
    public TitleFilter newInstance() {
        return new TitleFilter();
    }

    public String[] varNames() {
        return new String[0];
    }
}