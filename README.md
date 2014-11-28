rapaio-java
===========
Statistics, data mining and machine learning toolbox in Java

See tutorials [Rapaio Tutorials](http://padreati.github.io/rapaio/)

Implemented Features
====================
Implemented features are described by category. For each feature there are some
notes regarding the development stage. If there are no notes it means the feature
is considered to be fully implemented and well tested.

**Core Statistics**

* Special Math functions
* Maximum, Minimum, Mode (only for nominal values), Sum, Mean, Variance
* Quantiles
* Root Mean Squared Error
* Median Absolute Error
* Receiver Operator Characteristic - ROC curves and ROC Area
* Confusion Matrix
* Online Statistics: minimum, maximum, count, mean, variance, standard deviation, skewness, kurtosis

**Distributions**

* Normal Distribution
* StudentT - not tested
* Continuous Uniform
* Discrete Uniform
* Bernoulli
* Binomial

**Correlations**

* Pearson product-moment coefficient
* Spearman's rank correlation coefficient

**Sampling**

StatSampling

* generates discrete integer samples with/without replacement, weighted/non-weighted
* offers utility methods for bootstraps, simple random, stratified sampling

**Classification**

* Decision Trees: DecisionStump
* Decision Trees: ID3
* Decision Trees: C45 (no pruning)
* Decision Trees: CART (no pruning)
* Rule: OneRule
* Boosting: AdaBoost.SAMME
* Ensemble: Bagging
* Ensemble: Random Forests
* Bayesian: NaiveBayes (Gaussian, Empirical, Multinomial)

**Regression**

* Simple: L1Regressor
* Simple: L2Regressor
* Simple: RandomValueRegressor
* Simple: ConstantRegressor
* Tree: CART (no pruning)
* Tree: C45 (no pruning)
* Tree: DecisionStump

Not Implemented Features
========================

An important refactor is in progress. As a consequence, all the algorithm implementations
are considered by default in experimental state.


* Linear Regressor - filter only numerical attributes
* MultiLayer Perceptron - regression with sigmoid
* Gradient Boosting Tree regressor

**Matrices and vectors**

* Numeric vector operations
* Basic matrix operations and matrix decompositions

**Graphics**

All the graphics components are in usable state. However the graphics customization needs
further improvements in order to make the utilization easier.

* Plot histogram
* Plot density line
* Plot points
* Plot lines
* Plot function
* Plot vertical/horizontal/ab line
* Plot legend
* Plot ROC Curve
* QQPlot
* BarChart
* BoxPlot


** Other notes **

Check JDistLib for distribution function implementations
Added links to rcaller in order to test against R implementations.