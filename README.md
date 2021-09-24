
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
 
[![Launch rapaio with IJava binder jupyter](images/launch-binder.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master) 
[![Launch rapaio with IJava binder jupyter lab](images/launch-binder-lab.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master?urlpath=lab)
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
written as Jupyter notebooks and hosted at
[rapaio-notebooks github repository](https://github.com/padreati/rapaio-notebooks). The notebooks repository can also be spin up
through [binder]().

The complete list of features is presented [here](https://padreati.github.io/rapaio/complete-library-features/). An incomplete list of
implemented algorithms and features includes: core statistical tools, common distributions and hypothesis testing, Naive Bayes, Binary
Logistic Regression, Decision Trees (regression and classification), Random Forests (regression and classification), AdaBoost, Gradient
Boosting Trees (regression and classification), BinarySMO SVM, Relevant Vector Machines (regression), Linear and Ridge Regression, PCA and
KMeans. Additionaly there is a fair share of graphical tools and linear algebra stuff. And the list is growing periodically.

## Installation

Last published release on maven central is 3.0.0

       <dependency>
         <groupId>io.github.padreati</groupId>
         <artifactId>rapaio</artifactId>
         <version>3.0.0</version>
       </dependency>

The best way for exploration is through jupyter / jupyter-lab notebooks. This is excellent for experimenting with interactive notebooks or
to document the ideas you are working on. You have to install jupyter / jupyter-lab and IJava kernel. For more information you can follow
the instruction from
[IJava](https://github.com/SpencerPark/IJava#installing). The following notation is specific to IJava kernel jupyter notation.

    %maven io.github.padreati:rapaio:3.0.0  

## Acknowledgements

Many thanks to 
[![JetBrains](images/jetbrains-variant-3_logos/jetbrains-variant-3.svg)](https://www.jetbrains.com/?from=rapaio) 
who provided open source licenses for their brilliant IDE 
[![a](images/intellij-idea_logos/logo.svg)](https://www.jetbrains.com/?from=rapaio)
 
Many thanks to [SpencerPark](https://github.com/SpencerPark) for the java kernel he realized 
[IJava jupyter kernel](https://github.com/SpencerPark/IJava).

