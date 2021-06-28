/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/21.
 */
module rapaio {
    requires java.desktop;
    requires java.logging;
    requires java.sql;

    exports rapaio.core;
    exports rapaio.core.correlation;
    exports rapaio.core.distributions;
    exports rapaio.core.distributions.empirical;
    exports rapaio.core.stat;
    exports rapaio.core.tests;
    exports rapaio.core.tools;

    exports rapaio.data;
    exports rapaio.data.filter;
    exports rapaio.data.format;
    exports rapaio.data.ops;
    exports rapaio.data.stream;
    exports rapaio.data.group;
    exports rapaio.data.index;
    exports rapaio.data.mapping;
    exports rapaio.data.unique;

    exports rapaio.datasets;

    exports rapaio.graphics;

    exports rapaio.math;
    exports rapaio.math.linear;
    exports rapaio.math.linear.base;
    exports rapaio.math.linear.dense;
    exports rapaio.math.linear.decomposition;
    exports rapaio.math.functions;
    exports rapaio.math.optimization;

    exports rapaio.printer;
    exports rapaio.printer.opt;
    exports rapaio.printer.local;
    exports rapaio.printer.standard;

    exports rapaio.util;
    exports rapaio.util.collection;
    exports rapaio.util.function;
    exports rapaio.util.hash;
}