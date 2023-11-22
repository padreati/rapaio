module rapaio.collections {

    requires java.logging;
    requires jdk.incubator.vector;

    requires rapaio.commons;

    exports rapaio.math;

    exports rapaio.math.tensor;
    exports rapaio.math.tensor.dtype;
    exports rapaio.math.tensor.mill;
    exports rapaio.math.tensor.iterators;
    exports rapaio.math.tensor.layout;
    exports rapaio.math.tensor.operator;

    exports rapaio.util;
    exports rapaio.util.collection;
    exports rapaio.util.function;
    exports rapaio.util.hash;
    exports rapaio.util.parralel;
    exports rapaio.util.time;

}