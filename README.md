rapaio
===========
Statistics, data mining and machine learning toolbox in Java

See tutorials [Rapaio Tutorials](http://padreati.github.io/rapaio/)

Implemented Features
====================
For each feature there are some
notes regarding the development stage. If there are no notes it means the feature
is considered to be fully implemented and well tested.

**Core Statistics**

* Special Math functions
* Maximum, Minimum, Mode (only for nominal values), Sum, Mean, Variance
* Quantiles
* Online Statistics: minimum, maximum, count, mean, variance, standard deviation, skewness, kurtosis

**Correlations**

* Pearson product-moment coefficient
* Spearman's rank correlation coefficient

**Distributions**

* Continuous/Discrete Uniform
* Bernoulli
* Binomial
* Normal/Gaussian
* StudentT
* ChiSquare
* Gamma
* Empirical KDE (gaussian, epanechnikov, cosine, tricube, biweight, triweight, triangular, uniform)

**Sampling**

* SamplingTool
    * generates discrete integer samples with/without replacement, weighted/non-weighted
    * offers utility methods for bootstraps, simple random, stratified sampling
* Samplers used in machine learning algorithms

**Classification**
* Bayesian: NaiveBayes (Gaussian, Empirical, Multinomial)


Experminental Stage Features
============================

**Core Statistics**

* Root Mean Squared Error
* Median Absolute Error
* Receiver Operator Characteristic - ROC curves and ROC Area
* Confusion Matrix

**Hypothesis Testing**

* ChiSquare goodness of fit / independence test
* Kolmogorov Smirnoff one/two sample test


**Classification**

* Decision Trees: DecisionStump
* Decision Trees: ID3
* Decision Trees: C45 (no pruning)
* Decision Trees: CART (no pruning)
* Rule: OneRule
* Boosting: AdaBoost.SAMME
* Boosting: GBT (Gradient Boosting Trees) Classifier
* Ensemble: SplitClassifier
* Ensemble: Bagging
* Ensemble: Random Forests
* SVM: BinarySMO
* Linear: BinaryLogistic

**Regression**

* Simple: L1Regressor
* Simple: L2Regressor
* Simple: RandomValueRegressor
* Simple: ConstantRegressor
* Tree: CART (no pruning)
* Tree: C45 (no pruning)
* Tree: DecisionStump
* Boost: GBT (Gradient Boosting Tree) Regressor
* NNet: MultiLayer Perceptron Regressor
* Function: OLSRegressor (one target, only numerical attributes, no summary)

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
