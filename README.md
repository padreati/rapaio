rapaio
===========
Statistics, data mining and machine learning toolbox in Java.

[Rapaio Manual index page](manual/index.Md)


Implemented Features
====================
For each feature there are some
notes regarding the development stage. If there are no notes it means the feature
is considered to be fully implemented and well tested.

**Core Statistics**

* Special Math functions
* Maximum, Minimum, Mode (only for nominal values), Sum, Mean, Variance, Quantiles
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

**Hypothesis Testing**

* Pearson Chi-Square goodness of fit / independence test

**Analysis**

* Principal Components Analysis
* Fischer Linear Discriminant Analysis

**Classification**

* Bayesian: NaiveBayes (GaussianPdf, EmpiricalPdf, MultinomialPmf)
* Linear: BinaryLogistic
* Rule: OneRule
* Decision Trees - CTree: DecisionStump, ID3, C45, CART
  * purity: entropy, infogain, gain ration, gini index
  * weight on instances
  * split: numeric binary, nominal binary, nominal full
  * missing value handling: ignore, random, majority, weighted
  * reduced-error pruning
  * variable importance: frequency, gain and permutation based
* Ensemble: CForest - Bagging, Random Forests
* Boosting: AdaBoost.SAMME
* SVM: BinarySMO (Platt)

**Clusterization**

* KMeans clustering

**Graphics**

* QQ Plot
* Box Plot
* 2d Histogram


Experminental Stage Features
============================

**Core Statistics**

* Root Mean Squared Error
* Median Absolute Error
* Receiver Operator Characteristic - ROC curves and ROC Area
* Confusion Matrix

**Hypothesis Testing**

* Kolmogorov Smirnoff one/two sample test


**Classification**

* Boosting: GBT (Gradient Boosting Trees) Classifier
* Ensemble: SplitClassifier

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
* BarChart
