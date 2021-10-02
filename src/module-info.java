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
    exports rapaio.data.ops;
    exports rapaio.data.sample;
    exports rapaio.data.stream;
    exports rapaio.data.unique;

    exports rapaio.datasets;

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
    exports rapaio.ml.common.kernel;
    exports rapaio.ml.common.kernel.cache;

    exports rapaio.ml.loss;

    exports rapaio.ml.eval;
    exports rapaio.ml.eval.metric;
    exports rapaio.ml.eval.split;

    exports rapaio.ml.analysis;

    exports rapaio.ml.clustering;
    exports rapaio.ml.clustering.km;

    exports rapaio.ml.classifier;
    exports rapaio.ml.classifier.linear;
    exports rapaio.ml.classifier.linear.binarylogistic;
    exports rapaio.ml.classifier.bayes;
    exports rapaio.ml.classifier.bayes.nb;
    exports rapaio.ml.classifier.svm;
    exports rapaio.ml.classifier.rule;
    exports rapaio.ml.classifier.rule.onerule;
    exports rapaio.ml.classifier.tree;
    exports rapaio.ml.classifier.tree.ctree;
    exports rapaio.ml.classifier.boost;
    exports rapaio.ml.classifier.ensemble;

    exports rapaio.ml.regression;
    exports rapaio.ml.regression.linear;
    exports rapaio.ml.regression.linear.impl;
    exports rapaio.ml.regression.simple;
    exports rapaio.ml.regression.tree;
    exports rapaio.ml.regression.tree.rtree;
    exports rapaio.ml.regression.boost;
    exports rapaio.ml.regression.rvm;
    exports rapaio.ml.regression.ensemble;

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
}