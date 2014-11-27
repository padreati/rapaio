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

* DiscreteSampling - generates discrete integer samples with/without replacement, weighted/non-weighted
* StatSampling - offers utility methods for bootstraps, simple random, stratified sampling

**Classification**

* TreeClassifier (DecisionStump, ID3, C45, CART)
* AdaBoost.SAMME
* Forests (Bagging, Random Forests)
* NaiveBayes (Gaussian estimation, KDE estimation, Multinomial estimator)

**Regression**
* Simple regression algorithms: L1Regressor,L2Regressor, RandomValueRegressor, ConstantRegressor
* Tree Regressor - has no pruning (CART, C45, DecisionStump)

**Classification and Regression - To be Done **

An important refactor is in progress. As a consequence, all the algorithm implementations
are considered by default in experimental state.

* OneRule

* Linear Regressor - filter only numerical attributes
* MultiLayer Perceptron - regression with sigmoid
* Decision Stump Regressor
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