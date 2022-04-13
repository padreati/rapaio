package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.sortAsc;
import static rapaio.sys.With.sortDesc;
import static rapaio.sys.With.stacked;
import static rapaio.sys.With.top;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.ChiSquare;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.artist.AbstractArtistTest;

public class BarPlotTest extends AbstractArtistTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void testBarPlot() throws IOException {
        var mush = Datasets.loadMushrooms();

        GridLayer grid = gridLayer(2, 2);
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(false)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(true)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(false), sortDesc()));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(true), sortAsc(), top(5)));

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
                        v.addLabel(categories[RandomSource.nextInt(categories.length)]);
                    }
                    return v;
                },
                size -> {
                    VarBinary categories = VarBinary.empty().name("category");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(RandomSource.nextDouble() > 0.5 ? 1 : 0);
                    }
                    return categories;
                },
                size -> {
                    VarInt categories = VarInt.empty().name("categories");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(RandomSource.nextInt(4) + 1);
                    }
                    return categories;
                }
        };

        VarFactory[] conditionFactories = new VarFactory[] {
                size -> {
                    String[] categories = new String[] {"cond1", "cond2", "cond3"};
                    VarNominal v = VarNominal.empty(0, "condition");
                    for (int i = 0; i < size; i++) {
                        v.addLabel(categories[RandomSource.nextInt(categories.length)]);
                    }
                    return v;
                },
                size -> {
                    VarBinary categories = VarBinary.empty().name("condition");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(RandomSource.nextDouble() > 0.5 ? 1 : 0);
                    }
                    return categories;
                },
                size -> {
                    VarInt categories = VarInt.empty().name("condition");
                    for (int i = 0; i < size; i++) {
                        categories.addInt(RandomSource.nextInt(4) + 1);
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
