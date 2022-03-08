open module rapaio {

    requires java.desktop;
    requires java.logging;
    requires java.sql;

    requires jdk.incubator.vector;

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
    exports rapaio.data.group;
    exports rapaio.data.group.function;
    exports rapaio.data.index;
    exports rapaio.data.mapping;
    exports rapaio.data.sample;
    exports rapaio.data.stream;
    exports rapaio.data.unique;

    exports rapaio.datasets;

    exports rapaio.finance.data;

    exports rapaio.graphics;
    exports rapaio.graphics.base;
    exports rapaio.graphics.opt;
    exports rapaio.graphics.plot;
    exports rapaio.graphics.plot.artist;

    exports rapaio.image;

    exports rapaio.io;

    exports rapaio.math;
    exports rapaio.math.functions;
    exports rapaio.math.linear;
    exports rapaio.math.linear.option;
    exports rapaio.math.linear.base;
    exports rapaio.math.linear.dense;
    exports rapaio.math.linear.decomposition;
    exports rapaio.math.optimization;
    exports rapaio.math.optimization.linesearch;
    exports rapaio.math.optimization.scalar;

    exports rapaio.ml.common;
    exports rapaio.ml.common.distance;
    exports rapaio.ml.common.kernel;
    exports rapaio.ml.common.kernel.cache;

    exports rapaio.ml.loss;

    exports rapaio.ml.eval;
    exports rapaio.ml.eval.metric;
    exports rapaio.ml.eval.split;

    exports rapaio.ml.analysis;


    exports rapaio.ml.model;
    exports rapaio.ml.model.bayes;
    exports rapaio.ml.model.bayes.nb;
    exports rapaio.ml.model.boost;
    exports rapaio.ml.model.ensemble;
    exports rapaio.ml.model.km;
    exports rapaio.ml.model.linear;
    exports rapaio.ml.model.linear.binarylogistic;
    exports rapaio.ml.model.linear.impl;
    exports rapaio.ml.model.rule;
    exports rapaio.ml.model.rule.onerule;
    exports rapaio.ml.model.rvm;
    exports rapaio.ml.model.simple;
    exports rapaio.ml.model.svm;
    exports rapaio.ml.model.tree;
    exports rapaio.ml.model.tree.ctree;
    exports rapaio.ml.model.tree.rtree;
    exports rapaio.ml.model.tree.rowpredicate;

    exports rapaio.printer;
    exports rapaio.printer.opt;
    exports rapaio.printer.local;
    exports rapaio.printer.standard;

    exports rapaio.sys;

    exports rapaio.ts;

    exports rapaio.util;
    exports rapaio.util.collection;
    exports rapaio.util.function;
    exports rapaio.util.hash;
    exports rapaio.ml.model.svm.libsvm;
    exports rapaio.experiment.core;
    exports rapaio.printer.idea;
}