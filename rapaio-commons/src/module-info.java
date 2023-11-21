module rapaio.commons {

    requires java.desktop;
    requires java.logging;

    exports rapaio.printer;
    exports rapaio.printer.opt;
    exports rapaio.printer.nparam;
    exports rapaio.printer.local;
    exports rapaio.printer.idea;
    exports rapaio.printer.standard;

    exports rapaio.sys;
}