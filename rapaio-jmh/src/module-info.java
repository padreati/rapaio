open module rapaio.jmh {
    requires java.desktop;
    requires jmh.core;
    requires jmh.generator.annprocess;
    requires jdk.incubator.vector;
    requires jdk.unsupported;
    requires jdk.incubator.concurrent;

    requires rapaio.core;
    requires rapaio.experimental;
}