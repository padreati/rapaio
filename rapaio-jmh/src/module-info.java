open module rapaio.jmh {
    requires java.base;
    requires java.desktop;
    requires jmh.core;
    requires jmh.generator.annprocess;
    requires jdk.incubator.vector;
    requires jdk.unsupported;

    requires rapaio.lib;
    requires rapaio.experiment;
    requires JSAT;
}