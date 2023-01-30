module rapaio.experimental {

    requires java.desktop;
    requires java.sql;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.swing;

    requires jdk.incubator.vector;

    requires rapaio.core;

    exports rapaio.experiment.math.tensor.storage;
}