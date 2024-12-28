open module rapaio.experiment {

    requires java.logging;
    requires java.sql;

    requires jdk.incubator.vector;

    requires rapaio.lib;

    requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires jdk.jdi;

    exports rapaio.experiment.fx;
    exports rapaio.experiment.math.linear;
    exports rapaio.experiment.math.linear.base;
    exports rapaio.experiment.math.linear.dense;
    exports rapaio.experiment.math.linear.decomposition;
}