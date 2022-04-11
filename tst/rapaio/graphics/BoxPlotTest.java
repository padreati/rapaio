package rapaio.graphics;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.fill;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.stream.VSpot;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;

public class BoxPlotTest extends AbstractArtistTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void testBoxPlot() throws IOException {
        Frame df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        Var x = df.rvar(1);
        Var factor = df.rvar("class");

        GridLayer grid = new GridLayer(2, 2);
        grid.add(boxplot(x, factor, fill(10, 50, 100)));

        Map<String, List<Double>> map = x.stream().collect(groupingBy(s -> factor.getLabel(s.row()), mapping(VSpot::getDouble, toList())));
        String[] names = factor.levels().stream().filter(map::containsKey).toArray(String[]::new);
        Var[] vars = Arrays.stream(names).map(name -> VarDouble.copy(map.get(name)).name(name)).toArray(Var[]::new);
        grid.add(boxplot(vars, fill(2, 3, 4)));

        grid.add(boxplot(SolidFrame.byVars(vars), fill(1,2,3)));

        grid.add(boxplot(vars[0], fill(3)));

        assertTest(grid, "boxplot-test");
    }
}
