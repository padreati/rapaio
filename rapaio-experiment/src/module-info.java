module rapaio.experiment {

    requires java.logging;
    requires java.sql;

    requires rapaio.commons;
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

}