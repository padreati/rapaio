module rapaio.experimental {

    requires java.desktop;
    requires java.sql;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.swing;

    requires jdk.incubator.vector;

    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.tree.analysis;

    requires rapaio.core;

    exports rapaio.experiment.asm;
}