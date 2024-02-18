module rapaio.experiment {

    requires java.logging;
    requires java.sql;

    requires jdk.incubator.vector;

    requires rapaio.core;

    requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;

    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.tree.analysis;
    requires org.objectweb.asm.util;

    exports rapaio.experiment.asm;

    exports rapaio.experiment.math.linear;
    exports rapaio.experiment.math.linear.base;
    exports rapaio.experiment.math.linear.dense;
    exports rapaio.experiment.math.linear.decomposition;
}