
<table style="border: none">
<tr><td>

![rapaio logo](./docs/logo/logo-medium.png)
</td>
<td>

## Disambiguation

1.  (Italian dictionary) *Field of turnips. It is also a place 
where there is confusion, where tricks and sims are plotted.*

2.  (Computer science) *Statistics, data mining and machine learning 
library written in Java.*
</td>
</tr>
</table>
<table style="border: none">
<tr>
<td>

### Try it online
 
[![Launch rapaio with rapaio-jupyter-kernel jupyter binder](images/launch-binder.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master) 
[![Launch rapaio with rapaio-jupyter-kernel jupyter-lab binder](images/launch-binder-lab.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master?urlpath=lab)
</td>
<td>

### Build status

[![build](https://github.com/padreati/rapaio/actions/workflows/maven.yml/badge.svg)](https://github.com/padreati/rapaio/actions/workflows/maven.yml/badge.svg)
[![codecov.io](https://codecov.io/github/padreati/rapaio/coverage.svg?branch=master)](https://codecov.io/github/padreati/rapaio?branch=master)
</td>
</tr>
</table>

## Documentation

[Rapaio](https://padreati.github.io/rapaio/) is a rich collection of data mining, statistics and machine learning tools written completely
in Java. Documentation for this library is hosted as [github pages](https://padreati.github.io/rapaio/). Most of the documentation is
written as Jupyter notebooks and can be seen at
[rapaio-book github repository](https://github.com/padreati/rapaio-book).

The complete list of features is presented [here](https://padreati.github.io/rapaio/complete-library-features/). An incomplete list of
implemented algorithms and features includes: core statistical tools, common distributions and hypothesis testing, Naive Bayes, Binary
Logistic Regression, Decision Trees (regression and classification), Random Forests (regression and classification), AdaBoost, Gradient
Boosting Trees (regression and classification), BinarySMO, SVM, Relevant Vector Machines (regression), Linear and Ridge Regression, PCA and
KMeans. Additionally there is a fair share of graphical tools and linear algebra stuff. And the list is growing periodically.

## Installation

Last published release on maven central is 7.0.1

    <dependency>
        <groupId>io.github.padreati</groupId>
        <artifactId>rapaio-lib</artifactId>
        <version>7.0.1</version>
    </dependency>

The best way for exploration is through jupyter / jupyter-lab notebooks. This is excellent for experimenting with interactive notebooks or
to document the ideas you are working on. You have to install `jupyter` / `jupyter-lab` and `rapaio-jupyter-kernel` kernel. 
For more information you can follow the instruction from
[Rapaio Jupyter Kernel](https://github.com/padreati/rapaio-jupyter-kernel#installation). 

    %dependency /add io.github.padreati:rapaio-lib:7.0.1
    %dependency /resolve

## Acknowledgements

Many thanks to **JetBrains** who provided open source licenses for their brilliant IDE 
[![a](images/intellij-idea_logos/logo.svg)](https://www.jetbrains.com/?from=rapaio).

