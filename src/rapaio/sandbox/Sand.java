package rapaio.sandbox;

import rapaio.WS;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.CBenchmark;
import rapaio.datasets.CTask;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.LocalPrinter;
import rapaio.ws.Summary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Sand {
    public static void main(String[] args) throws IOException {

        WS.setPrinter(new LocalPrinter());

        Frame df = BaseFilters.retainNominal(Datasets.loadMushrooms());
        String[] targetVars = df.varNames();

        Map<String, String[]> dict = new HashMap<>();
        Arrays.stream(targetVars).forEach(s -> dict.put(s, df.var(s).dictionary()));

        System.out.println(dict.keySet());

        Map<String, String[]> dict2 = Arrays.stream(targetVars).collect(Collectors.toMap(s->s, s->df.var(s).dictionary()));

        System.out.println("=============");
        for(String key: dict.keySet()) {
            System.out.println(key + Arrays.toString(dict.get(key)));
        }
        System.out.println("=============");
        for(String key: dict2.keySet()) {
            System.out.println(key + Arrays.toString(dict2.get(key)));
        }
    }

}
