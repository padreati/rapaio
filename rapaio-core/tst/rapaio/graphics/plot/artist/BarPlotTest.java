/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.barplot;
import static rapaio.graphics.Plotter.gridLayer;
import static rapaio.graphics.opt.GOpts.sort;
import static rapaio.graphics.opt.GOpts.stacked;
import static rapaio.graphics.opt.GOpts.top;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.ChiSquare;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;

public class BarPlotTest extends AbstractArtistTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testBarPlot() throws IOException {
        var mush = Datasets.loadMushrooms();

        GridLayer grid = gridLayer(2, 2);
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(false)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(true)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(false), sort.desc()));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(true), sort.asc(), top(5)));

        assertTest(grid, "barplot-test");
    }

    interface VarFactory {
        Var generate(int size);
    }

    @Test
    void testRandom() throws IOException {
        int records = 100;

        VarFactory[] categoryFactories = new VarFactory[] {
                size -> {
                    String[] categories = new String[] {"categ1", "categ2"};
                    VarNominal v = VarNominal.empty(0, "category");
                    for (int i = 0; i < size; i++) {
                        v.addLabel(categories[random.nextInt(categories.length)]);
                    }
                    return v;
                },
                size -> {
                    VarBinary categories = VarBinary.empty().name("category");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(random.nextDouble() > 0.5 ? 1 : 0);
                    }
                    return categories;
                },
                size -> {
                    VarInt categories = VarInt.empty().name("categories");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(random.nextInt(4) + 1);
                    }
                    return categories;
                }
        };

        VarFactory[] conditionFactories = new VarFactory[] {
                size -> {
                    String[] categories = new String[] {"cond1", "cond2", "cond3"};
                    VarNominal v = VarNominal.empty(0, "condition");
                    for (int i = 0; i < size; i++) {
                        v.addLabel(categories[random.nextInt(categories.length)]);
                    }
                    return v;
                },
                size -> {
                    VarBinary categories = VarBinary.empty().name("condition");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(random.nextDouble() > 0.5 ? 1 : 0);
                    }
                    return categories;
                },
                size -> {
                    VarInt categories = VarInt.empty().name("condition");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(random.nextInt(4) + 1);
                    }
                    return categories;
                }
        };

        VarFactory[] valueFactories = new VarFactory[] {
                size -> VarDouble.fill(size, ChiSquare.of(1).sampleNext() * 20)
        };

        for (VarFactory categoryFactory : categoryFactories) {
            Var categ = categoryFactory.generate(records);
            for (VarFactory conditionFactory : conditionFactories) {
                Var cond = conditionFactory.generate(records);
                for (VarFactory valueFactory : valueFactories) {
                    Var value = valueFactory.generate(records);

                    GridLayer gl = gridLayer(2, 2);
                    gl.add(barplot(categ, cond, value));
                    gl.add(barplot(categ, null, value));
                    gl.add(barplot(categ, cond));
                    gl.add(barplot(categ));
                    assertTest(gl, "barplot-types-" + categ.type().code() + "_" + cond.type().code() + "_" + value.type().code());
                }
            }
        }
    }
}
