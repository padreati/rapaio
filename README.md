rapaio
======

Statistics, data mining and machine learning toolbox

See tutorials [Rapaio Tutorials](http://padreati.github.io/rapaio/)

Implemented Features
====================

Implemented features are described by category. For each feature there are some
notes regarding the development stage. If there are no notes it means the feature
is considered to be fully implemented and well tested.

**Core Statistics**
* Base Math functions
* Special Math functions
* Maximum
* Minimum
* Mode (only for nominal values)
* Quantiles
* Root Mean Squared Error
* ROC (Receiver Operator Characteristic) - ROC curves and ROC Area
* Sum
* Variance
* Confusion Matrix

**Online Core Statistics**
* Minimum
* Maximum
* Count
* Mean
* Variance
* Standard Deviation
* Skewness
* Kurtosis

**Distributions**
* Normal Distribution
* StudentT - not tested
* Continuous Uniform
* Discrete Uniform

**Correlations**
* Pearson product-moment coefficient
* Spearman's rank correlation coefficient

**Classification**
* One Rule
* Decision Stump
* AdaBoost SAMME
* Random Tree (used in Random Forest)
* Random Forests uses a variant of tree similar with C45 (in the future will be implemented with CART)
* ID3 - additional option to use missing values as category
* C45 Classifier (incomplete)

**Regression**

* Tree Regressor - accept only numerical, non-missing, has no pruning (with this constraints
this tree is similar with CART and C45)
* Linear Regressor - filter only numerical attributes

**Matrices and vectors**
* Numeric vector operations
* Basic matrix operations and matrix decompositions

Feature Journal
===============

This journal contains info regarding the history of implementation. Basically here we can follow
the progress of the implementation:

16-Feb-2014 - Ported matrix operations and decompositions from JAMA project to rapaio.
16-Feb-2014 - Implemented numeric vector operations. It follows the design from R.