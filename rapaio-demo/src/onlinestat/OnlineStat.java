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

package onlinestat;

import rapaio.core.distributions.Poisson;
import rapaio.data.Var;
import rapaio.data.VarDouble;

public class OnlineStat {

    public static void main(String[] args) {

        Poisson poisson = Poisson.of(1276312);
        int n1 = 100000000;
        int n2 = 2000000;
        Var x1 = VarDouble.from(n1, () -> poisson.sampleNext()).name("x1");
        Var x2 = VarDouble.from(n2, () -> poisson.sampleNext()).name("x2");

        Var x = x1.bindRows(x2).copy().name("x");

        System.out.println("var[x]=" + x.dv().variance()*(n1+n2-1)/(n1+n2));

        double x1sqSum = x1.dvNew().apply(v -> v * v).sum();
        double x2sqSum = x2.dvNew().apply(v -> v * v).sum();

        System.out.println(x.dvNew().apply(v->v*v).sum()/(n1+n2) - x.dv().mean()*x.dv().mean());
        System.out.println(
                (x1sqSum + x2sqSum) / (n1 + n2) - Math.pow(n1 * x1.dv().mean() / (n1 + n2) + n2 * x2.dv().mean() / (n1 + n2), 2));

    }
}
